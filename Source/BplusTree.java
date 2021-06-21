import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BplusTree {
	int degree;
	Node root;
	
	BplusTree(int degree){
		this.degree = degree;
		this.root = new LeafNode(degree); // root는 처음에 리프노드 
	}
	
	void insert(int key, int value) {
		if(root.getClass() == LeafNode.class) { // root가 LeafNode일 때  
			Node[] nodes = root.insert(key, value);
			if(nodes[1] != null) { // root 노드 -> NonLeafNode로 승격 
				NonLeafNode newRoot = new NonLeafNode(degree);
				
				int tmpKey = ((LeafNode)nodes[1]).keys.get(0); // 오른쪽 노드의 맨 왼쪽 값 가져옴 
				
				newRoot.keys.add(tmpKey);
				newRoot.childNodes.add(nodes[0]);
				newRoot.childNodes.add(nodes[1]);
				
				root = newRoot;
			}
		} else { // root가 NonLeafNode일 때 
			Node[] nodes = root.insert(key, value);
			if(nodes[1] != null) { // root 노드 -> 위로 한칸 올라감  
				NonLeafNode newRoot = new NonLeafNode(degree);
				
				newRoot.keys.add(nodes[1].nlSplitKey);
				newRoot.childNodes.add(nodes[0]);
				newRoot.childNodes.add(nodes[1]);
				nodes[1].nlSplitKey = -1;
				
				root = newRoot;
			}
		}
	}
	void delete(int key) { 
		int deleteIdx = root.delete(key, null, root); // root의 부모는 null, tmpRoot는 root 
		
		if(deleteIdx != -1) { // 삭제할 인덱스가 남아있는 상태 
			// 여기 들어온다는 뜻은 리프노드면 리프노드에서 이미 삭제 진행하고 인덱스 남아있지 않으므로 무조건 논리프노드 
			NonLeafNode newRoot = (NonLeafNode) root;
			newRoot.keys.remove(deleteIdx);
			
			// 루트에 키가 없는 경우 
			if(newRoot.keys.size() == 0) root = newRoot.childNodes.get(0); // 맨 왼쪽 child 노드를 가지고 온 다음 그 노드를 루트로 줌 
			else root = newRoot;
		}
		
		if(root.keys.size() == 0) root = new LeafNode(degree); // 루트가 비어있는 경우 
		
		// 논리프노드에서 삭제되지 않은 키를 위해 삭제를 한번 더 진행하여 키 업데이트 (리프노드의 키 바꿔주는 알고리즘 이용하기 위해)  
		root.delete(key, null, root); 
	}
	
	void singleKeySearch(int findKey) { root.singleKeySearch(findKey); }
	void rangedSearch(int startKey, int endKey) { root.rangedSearch(startKey, endKey); }
	
	void print() { 
		root.print();
	}
	
	ArrayList<Node> getNode(ArrayList<Node> nodes){
		nodes = root.getNode(nodes);
		return nodes;
	}
	
	void writeFile(String indexFile) {
		// 저장방식 
		// 1. degree 저장 
		// 2. Node 갯수 저장 
		// 3. Leaf, NonLeaf 구분하여 각각 저장 
		//	Leaf 저장해야할 정보: 0(리프노드), keys.size(), (키와, 밸류 각각) * 리스트 크기만큼, rSiblingNode가 가리키고 있는 노드의 index  
		//	NonLeaf 저장해야할 정보: 1(논리프노드), keys.size(), (키와, childNodes에 있는 노드의 index 각각) * 리스트 크기만큼, childNodes의 맨 마지막 child의 index  
		DataOutputStream writer = null;
		
		try {
			writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
			writer.writeInt(degree); // 1. degree 정보 저장  
			
			ArrayList<Node> nodes = new ArrayList<Node>();
			nodes = getNode(nodes); // 노드 이 ArrayList에 모두 삽입 
			writer.writeInt(nodes.size()); // 2. Node 갯수 저장 
			
			for(Node node: nodes) {
				if(node.getClass() == LeafNode.class) { // 3-1. 리프노드 
					writer.writeInt(0); // 리프노드 
					LeafNode tmpNode = (LeafNode) node;
					writer.writeInt(tmpNode.keys.size()); // keys.size()
					for(int i=0; i<tmpNode.keys.size(); i++) {
						writer.writeInt(tmpNode.keys.get(i)); // 키 
						writer.writeInt(tmpNode.values.get(i)); // 밸류 
					}
					writer.writeInt(nodes.indexOf(tmpNode.rSiblingNode)); // rSiblingNode가 가리키고 있는 노드의 index 
				} else if(node.getClass() == NonLeafNode.class) { // 3-2. 논리프노드 
					writer.writeInt(1); // 논리프노드 
					NonLeafNode tmpNode = (NonLeafNode) node;
					writer.writeInt(tmpNode.keys.size()); // keys.size()
					for(int i=0; i<tmpNode.keys.size(); i++) {
						writer.writeInt(tmpNode.keys.get(i)); // 키
						writer.writeInt(nodes.indexOf(tmpNode.childNodes.get(i))); // childNodes에 있는 노드의 index 
					}
					writer.writeInt(nodes.indexOf(tmpNode.childNodes.get(tmpNode.childNodes.size()-1))); // childNodes의 맨 마지막 child의 index 
				}
			}
		} catch (IOException e) {
			System.out.println("BplusTree.writeFile ERROR");
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				System.out.println("BplusTree.writeFile.writer.close() ERROR");
			}
		}
	}
}
