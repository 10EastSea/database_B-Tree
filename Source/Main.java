import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
	
	public static void main(String[] args) {
		if(args.length < 2) return;
		
		String cmd = args[0];
		String indexFile = args[1];
		
		if(cmd.equals("-c")) { // Creation 
			BplusTree bpt = new BplusTree(Integer.parseInt(args[2]));
			bpt.writeFile(indexFile);
		} else if(cmd.equals("-i")) { // Insertion / args[2] = data_file 
			BplusTree bpt = readFile(args[1]); // 파일 읽어오기 
			ArrayList<Pair> insertArr = readInsertFile(args[2]);
			for(Pair pair: insertArr) {
				bpt.insert(pair.key, pair.value);
			}
			bpt.writeFile(indexFile);
		} else if(cmd.equals("-d")) { // deletion / args[2] = data_file 
			BplusTree bpt = readFile(args[1]); // 파일 읽어오기 
			ArrayList<Integer> deleteArr = readDeleteFile(args[2]);
			for(Integer key: deleteArr) {
				bpt.delete(key);
			}
			bpt.writeFile(indexFile);
		} else if(cmd.equals("-s")) { // single key search / args[2] = key 
			BplusTree bpt = readFile(args[1]); // 파일 읽어오기 
			bpt.singleKeySearch(Integer.parseInt(args[2]));
		} else if(cmd.equals("-r")) { // ranged search / args[2] = start_key, args[3] = end_key 
			BplusTree bpt = readFile(args[1]); // 파일 읽어오기 
			bpt.rangedSearch(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		} else if(cmd.equals("-p")) {
			BplusTree bpt = readFile(args[1]); // 파일 읽어오기 
			bpt.print();
		}
	}
	
	public static BplusTree readFile(String indexFile) {
		BplusTree bpt = null;
		DataInputStream reader = null;
		
		try {
			reader = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
			
			ArrayList<Node> nodes = new ArrayList<Node>();
			int degree = reader.readInt(); // 1. degree 정보 읽기 
			bpt = new BplusTree(degree); 
			int size = reader.readInt(); // 2. 노드 갯수 읽기 
			
			// 각 노드별 인덱스 값 저장 
			// degree + 2은 (NonLeaf(-1)와 Leaf(rSiblingNode) 구분 , [node index ...](NonLeaf만)) 
			int[][] tmpArr = new int[size][degree+1];
			
			for(int i=0; i<size; i++) {
				int check = reader.readInt();
				if(check == 0) { // LeafNode 
					LeafNode tmpNode = new LeafNode(degree);
					int count = reader.readInt(); // 반복 횟수 (keys.size())
					
					for(int j=0; j<count; j++) {
						int tmpKey = reader.readInt();
						int tmpValue = reader.readInt();
						
						tmpNode.keys.add(tmpKey);
						tmpNode.values.add(tmpValue);
					}
					
					tmpArr[i][0] = reader.readInt(); // rSiblingNode이 가리키는 Node의 index 
					
					nodes.add(tmpNode);
				} else if(check == 1) { // NonLeafNode 
					NonLeafNode tmpNode = new NonLeafNode(degree);
					int count = reader.readInt(); // 반복 횟수 (keys.size())
					tmpArr[i][0] = count; // NonLeaf와 Leaf 구분 
					
					for(int j=1; j<=count; j++) {
						int tmpKey = reader.readInt();
						int tmpNodeIndex = reader.readInt();
						
						tmpNode.keys.add(tmpKey);
						tmpArr[i][j] = tmpNodeIndex; // childNodes가 가리키는 Node의 index 
					}
					
					tmpArr[i][count+1] = reader.readInt(); // childNodes의 맨 마지막 child가 가리키는 Node의 index 
					
					nodes.add(tmpNode);
				}
			}
			
			// 인덱싱 작업 
			int index = 0;
			for(Node node: nodes) {
				if(node.getClass() == LeafNode.class) { // LeafNode 
					LeafNode tmpNode = (LeafNode) node;
					
					if(index+1 == nodes.size()) continue; // 맨 마지막 노드 -> 얘는 무조건 리프노드에 rSiblingNode가 없음 
					tmpNode.rSiblingNode = (LeafNode) nodes.get(tmpArr[index][0]); // rSiblingNode 인덱싱 
				} else if(node.getClass() == NonLeafNode.class) { // NonLeafNode 
					NonLeafNode tmpNode = (NonLeafNode) node;
					
					int count = tmpArr[index][0];
					for(int i=1; i<=count; i++) {
						Node tmpChildNode = nodes.get(tmpArr[index][i]);
						tmpNode.childNodes.add(i-1, tmpChildNode); // childNodes가 가리키는 Node 인덱싱  
					}
					tmpNode.childNodes.add(nodes.get(tmpArr[index][count+1])); // rChildNode 인덱싱 
				}
				
				index++;
			}
			
			bpt.root = nodes.get(0); // root 인덱싱 
		} catch (IOException e) {
			System.out.println("Main.readFile ERROR");
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				System.out.println("Main.readFile.reader.close() ERROR");
			}
		}
		
		return bpt;
	}
	
	public static ArrayList<Pair> readInsertFile(String dataFile) {
		ArrayList<Pair> insertArr = new ArrayList<Pair>();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File(dataFile)));
			String line = "";
			
			while ((line = reader.readLine()) != null) { // key, value 저장
				String[] token = line.split(",");
				insertArr.add(new Pair(Integer.parseInt(token[0]), Integer.parseInt(token[1])));
			}
			
			reader.close();
		} catch (IOException e) {
			System.out.println("Main.readFileInsert ERROR");
		}
		
		return insertArr;
	}
	
	public static ArrayList<Integer> readDeleteFile(String dataFile) {
		ArrayList<Integer> deleteArr = new ArrayList<Integer>();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File(dataFile)));
			String line = "";
			
			while ((line = reader.readLine()) != null) { // key 저장
				deleteArr.add(Integer.parseInt(line));
			}
			
			reader.close();
		} catch (IOException e) {
			System.out.println("Main.readFileInsert ERROR");
		}
		
		return deleteArr;
	}

}
