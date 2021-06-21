import java.util.ArrayList;

public class LeafNode extends Node {
	ArrayList<Integer> values; // value 
	LeafNode rSiblingNode; // right sibling node
	
	LeafNode(int degree){
		this.degree = degree;
		this.keys = new ArrayList<Integer>(); // 얘내 둘(아래 values)은 갯수가 같아야함! 
		this.values = new ArrayList<Integer>();
		this.rSiblingNode = null;
	}

	Node[] insert(int key, int value) {
		Node[] nodes = new Node[2];
		nodes[0] = this;
		nodes[1] = null; // spilt이 되면 여기에 객체 넣고 리턴 
		
		int index = 0;
		for(; index < keys.size(); index++) {
			if(key < keys.get(index)) break; // Node 내에서 값은 linear search 
			else if(key == keys.get(index)) return nodes; // 중복된 키 
		}
		
		keys.add(index, key); // 키와 밸류 추가 
		values.add(index, value);
		
		if(keys.size() >= degree) nodes[1] = split(); // 노드가 꽉 차면 split!! 
	
		return nodes;
	}

	int delete(int key, Node parentNode, Node tmpRoot) {
		int parentIdx = 0; // 부모노드에서 child로 들어온 위치를 갖는 인덱스 
		if(parentNode != null) {
			NonLeafNode tmpParent = (NonLeafNode) parentNode;
			for(; parentIdx < tmpParent.childNodes.size(); parentIdx++) {
				if(tmpParent.childNodes.get(parentIdx) == this) break;
			}
		}
		
		int index = 0;
		for(; index < keys.size(); index++) {
			if(key == keys.get(index)) {
				keys.remove(index); // 키와 밸류 삭제 
				values.remove(index);
				break;
			}
		}
		
		if(this != tmpRoot && keys.size() < degree/2) { // 루트노드가 아닐때 언더플로우
			LeafNode siblingNode;
			
			if(parentIdx > 0) { // 왼쪽 sibling이 있는 경우 
				siblingNode = (LeafNode) ((NonLeafNode)parentNode).childNodes.get(parentIdx-1);
				return merge(siblingNode, this, parentNode);
			} else { // 왼쪽 sibling이 없는 경우 -> 오른쪽 sibling
				siblingNode = (LeafNode) ((NonLeafNode)parentNode).childNodes.get(parentIdx+1);
				return merge(this, siblingNode, parentNode);
			}
		} else if(keys.size() > 0) { // 사실상 나머지 경우  
			// 논리프노드에서 키가 바뀌지 않은 경우 고려해 키 업데이트 
			Node tmpNode = tmpRoot;
			int afterKey = keys.get(0);
			
			while(tmpNode != null) {
				if(tmpNode.getClass() == LeafNode.class) return -1;
				NonLeafNode thisNode = (NonLeafNode) tmpNode;
				
				int tmpIndex = 0;
				for(; tmpIndex < thisNode.keys.size(); tmpIndex++) {
					if(key < thisNode.keys.get(tmpIndex)) break;
					else if (key == thisNode.keys.get(tmpIndex)) {
						thisNode.keys.set(tmpIndex, afterKey);
						return -1;
					}
				}
				
				tmpNode = thisNode.childNodes.get(tmpIndex);
			}
		}
		
		return -1;
	}

	void singleKeySearch(int findKey) {
		for(int i=0; i<keys.size(); i++) {
			if(keys.get(i) == findKey) {
				System.out.println(values.get(i));
				return;
			}
		}
		
		System.out.println("NOT FOUND"); // 키가 없으면 이 문장 출력 
	}

	void rangedSearch(int startKey, int endKey) {
		LeafNode tmpNode = this; // 현재 노드 
		
		while(tmpNode != null) {
			for(int i=0; i<tmpNode.keys.size(); i++) {
				if(tmpNode.keys.get(i) >= startKey && tmpNode.keys.get(i) <= endKey) System.out.println(tmpNode.keys.get(i) + ", " + tmpNode.values.get(i));
				else if(tmpNode.keys.get(i) > endKey) return;
			}
			tmpNode = tmpNode.rSiblingNode; // 현재 노드 다 보고나서 다음 노드로 이동 
		}
	}

	Node split() { // 현재 노드를 두 개로 나눈 후, 새로 만든 오른쪽 노드를 반환 
		LeafNode newNode = new LeafNode(degree);
		
		int index = degree/2;
		int count = degree - index; // 반복 횟수 
		for(int i=0; i<count; i++) {
			newNode.keys.add(this.keys.get(index));
			newNode.values.add(this.values.get(index));
			
			this.keys.remove(index);
			this.values.remove(index);
		}
		
		newNode.rSiblingNode = this.rSiblingNode; // 현재 노드의 오른쪽 형제노드를 새로 만든 노드의 오른쪽 형제 노드로 줌 
		this.rSiblingNode = newNode; // 새로 만든 노드를 현재 노드의 오른쪽 형제노드로 함 
		
		return newNode;
	}

	int merge(Node leftSibling, Node rightSibling, Node parentNode) {
		LeafNode leftNode = (LeafNode) leftSibling;
		LeafNode rightNode = (LeafNode) rightSibling;
		
		int parentIdx = 0;
		
		if(leftNode.keys.size() + rightNode.keys.size() < degree) { // merge 가능 -> 왼쪽 노드에 다 때려넣음 
			leftNode.keys.addAll(rightNode.keys);
			leftNode.values.addAll(rightNode.values);
			leftNode.rSiblingNode = rightNode.rSiblingNode;
			
			parentIdx = ((NonLeafNode)parentNode).childNodes.indexOf(rightNode);
			((NonLeafNode)parentNode).childNodes.remove(rightNode); // 부모에서 right child 삭제 
			
			return parentIdx - 1;
		} else { // merge 불가능
			// merge가 불가능하면 왼쪽이나 오른쪽에서 키, 밸류 하나 주는 것으로 해결!! 
			if(leftNode.keys.size() < degree/2) { // 왼쪽노드가 언더플로우 
				// 오른쪽에서 키, 밸류 하나빼고 왼쪽에다 줌 
				int tmpChildIdx = ((NonLeafNode)parentNode).childNodes.indexOf(rightNode);
				
				int tmpKey = rightNode.keys.get(0);
				int tmpValue = rightNode.values.get(0);
				
				leftNode.keys.add(tmpKey);
				leftNode.values.add(tmpValue);
				
				rightNode.keys.remove(0);
				rightNode.values.remove(0);
				
				parentNode.keys.set(tmpChildIdx-1, ((NonLeafNode)parentNode).childNodes.get(tmpChildIdx).keys.get(0)); // 부모노드 키 업데이트 
			} else { // 오른쪽 노드가 언더플로우 
				// 왼쪽에서 키, 밸류 하나빼고 오른쪽에다 줌 
				int tmpChildIdx = ((NonLeafNode)parentNode).childNodes.indexOf(rightNode);
				
				int tmpKey = leftNode.keys.get(leftNode.keys.size()-1);
				int tmpValue = leftNode.values.get(leftNode.values.size()-1);
				
				rightNode.keys.add(0, tmpKey);
				rightNode.values.add(0, tmpValue);
				
				leftNode.keys.remove(leftNode.keys.size()-1);
				leftNode.values.remove(leftNode.values.size()-1);
				
				parentNode.keys.set(tmpChildIdx-1, ((NonLeafNode)parentNode).childNodes.get(tmpChildIdx).keys.get(0));// 부모노드 키 업데이트 
			}
		}
		
		return -1;
	}

	public String toString() {
		String str = "";
		for(int i=0; i<keys.size(); i++) {
			str += "(" + keys.get(i) + ", " + values.get(i) + ")";
		}
		return str;
	}
	
	void print() {
		System.out.println(this.toString());
	}
	
	ArrayList<Node> getNode(ArrayList<Node> nodes){
		nodes.add(this);
		return nodes;
	}
}
