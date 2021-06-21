import java.util.ArrayList;

public class NonLeafNode extends Node {
	ArrayList<Node> childNodes; // childNode들, right most child는 이 리스트의 맨 끝 값 

	NonLeafNode(int degree){
		this.degree = degree;
		this.keys = new ArrayList<Integer>();
		this.childNodes= new ArrayList<Node>();
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
		
		Node[] tmpNodes;
		tmpNodes = childNodes.get(index).insert(key, value);
		
		if(tmpNodes[1] != null && tmpNodes[1].getClass() == LeafNode.class) { // 리프에서 split 되었을 때 
			int tmpKey = ((LeafNode)tmpNodes[1]).keys.get(0);
			
			keys.add(index, tmpKey);
			childNodes.add(index+1, tmpNodes[1]);
		} else if(tmpNodes[1] != null && tmpNodes[1].getClass() == NonLeafNode.class) { // non리프에서 split 되었을 때 
			int tmpKey = tmpNodes[1].nlSplitKey; // 위로 올라간 키 받아준 것 => 이걸로 NonLeafNode 키 만들어주면 됨 
			tmpNodes[1].nlSplitKey = -1;
			
			keys.add(index, tmpKey);
			childNodes.set(index, tmpNodes[0]);
			childNodes.add(index+1, tmpNodes[1]);
		}
		
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
			if(key < keys.get(index)) break; // Node 내에서 값은 linear search 
		}
		
		int deleteIdx = -1; // 삭제해야할 인덱스가 남아있으면 여따 넣어줌 
		deleteIdx = childNodes.get(index).delete(key, this, tmpRoot);
		
		if(deleteIdx != -1) { // 삭제할 인덱스가 남아있다면 
			if(this == tmpRoot) return deleteIdx; // 현재 노드가 루트면 루트에서 삭제 
			
			keys.remove(deleteIdx); // 키 삭제 
			
			if(keys.size() < degree/2) { // 키가 부족하면 merge!!
				NonLeafNode siblingNode;
				
				if(parentIdx > 0) { // 왼쪽 sibling이 있는 경우 
					siblingNode = (NonLeafNode) ((NonLeafNode)parentNode).childNodes.get(parentIdx-1);
					return merge(siblingNode, this, parentNode);
				} else { // 왼쪽 sibling이 없는 경우 -> 오른쪽 sibling
					siblingNode = (NonLeafNode) ((NonLeafNode)parentNode).childNodes.get(parentIdx+1);
					return merge(this, siblingNode, parentNode);
				}
			}
		}
		
		return -1;
	}

	void singleKeySearch(int findKey) {
		int index = 0;
		for(int i=0; i<keys.size(); i++) {
			System.out.print(keys.get(i)); // 키 값들 출력 
			if(i+1 != keys.size()) System.out.print(", ");
			else System.out.println();
			
			if(findKey >= keys.get(i)) index++; // 찾고자 하는 위치 index 세팅 
		}
		
		childNodes.get(index).singleKeySearch(findKey);
	}

	void rangedSearch(int startKey, int endKey) {
		int index = 0;
		for(; index<keys.size(); index++) {
			if(startKey < keys.get(index)) break; // Node 내에서 값은 linear search 
		}
		
		childNodes.get(index).rangedSearch(startKey, endKey);
	}

	Node split() {
		NonLeafNode newNode = new NonLeafNode(degree);
		
		int index = degree/2 + 1;
		int count = degree - index; // 반복 횟수 
		
		for(int i=0; i<count; i++) {
			int tmpKey = this.keys.get(index);
			Node tmpChild = this.childNodes.get(index);
			
			newNode.keys.add(tmpKey);
			newNode.childNodes.add(tmpChild);
			
			this.keys.remove(index);
			this.childNodes.remove(index);
		}
		
		newNode.childNodes.add(this.childNodes.remove(index)); // 새로만든 오른쪽 노드에 맨 오른쪽 끝 child 추가 
		
		newNode.nlSplitKey = this.keys.get(index-1); // 위로 올려줄 key값 
		this.keys.remove(index-1);
		
		return newNode;
	}

	int merge(Node leftSibling, Node rightSibling, Node parentNode) {
		NonLeafNode leftNode = (NonLeafNode) leftSibling;
		NonLeafNode rightNode = (NonLeafNode) rightSibling;
		
		int parentIdx = 0;
		for(; parentIdx < parentNode.keys.size(); parentIdx++) { // 부모노드의 인덱스 찾기 
			NonLeafNode tmpParent = (NonLeafNode) parentNode;
			if(tmpParent.childNodes.get(parentIdx) == leftNode && tmpParent.childNodes.get(parentIdx+1) == rightNode) break;
		}
		
		int nlMergeKey = parentNode.keys.get(parentIdx); // merge 한 뒤에 부모노드에서 자식으로 내려줄 키 
		
		if(leftNode.keys.size() + rightNode.keys.size() < degree) { // merge 가능 
			leftNode.keys.add(nlMergeKey);
			leftNode.keys.addAll(rightNode.keys);
			leftNode.childNodes.addAll(rightNode.childNodes);
			
			NonLeafNode tmpParent = (NonLeafNode) parentNode;
			tmpParent.childNodes.remove(tmpParent.childNodes.indexOf(rightNode)); // 부모노드 자식 중 rightNode 삭제 
		
			if(leftNode.keys.size() >= degree) { // 여기서 오버플로우나면 split 해주어야 함 
				Node newNode = leftNode.split();
				
				int tmpKey = newNode.nlSplitKey;
				newNode.nlSplitKey = -1;
				
				int tmpIdx = ((NonLeafNode)parentNode).childNodes.indexOf(leftNode);
				((NonLeafNode)parentNode).keys.add(tmpIdx, tmpKey);
				((NonLeafNode)parentNode).childNodes.add(tmpIdx+1, newNode);
				
				return parentIdx+1;
			}
			
			return parentIdx;
		} else { // merge 불가능
			// merge가 불가능하면 왼쪽이나 오른쪽에서 키, 차일드 하나 주는 것으로 해결!! 
			if(leftNode.keys.size() < degree/2) { // leftNode가 언더플로우 
				// 오른쪽에서 키, 차일드 하나빼고 왼쪽에다 줌 
				int tmpKey = rightNode.keys.get(0);
				Node tmpChild = rightNode.childNodes.get(0);
				
				leftNode.keys.add(nlMergeKey);
				leftNode.childNodes.add(tmpChild);
				
				rightNode.keys.remove(0);
				rightNode.childNodes.remove(0);
				
				parentNode.keys.set(parentIdx, tmpKey); // 부모노드 키 업데이트 
			} else if(rightNode.keys.size() < degree/2) { // rightNode가 언더플로우 
				// 왼쪽에서 키, 차일드 하나빼고 오른쪽에다 줌  
				int tmpKey = leftNode.keys.get(leftNode.keys.size()-1);
				Node tmpChild = leftNode.childNodes.get(leftNode.childNodes.size()-1);
				
				rightNode.keys.add(0, nlMergeKey);
				rightNode.childNodes.add(0, tmpChild);
				
				leftNode.keys.remove(leftNode.keys.size()-1);
				leftNode.childNodes.remove(leftNode.childNodes.size()-1);
				
				parentNode.keys.set(parentIdx, tmpKey);  // 부모노드 키 업데이트 
			}
		}
		
		return -1;
	}
	
	public String toString() {
		String str = "";
		for(int i=0; i<keys.size(); i++) {
			str += "[" + keys.get(i) + "]";
		}
		return str;
	}
	
	void print() {
		System.out.println(this.toString());
		for(int i=0; i<childNodes.size(); i++) {
			childNodes.get(i).print();
		}
	}
	
	ArrayList<Node> getNode(ArrayList<Node> nodes) {
		nodes.add(this);
		for(int i=0; i<childNodes.size(); i++) {
			nodes = childNodes.get(i).getNode(nodes);
		}
		return nodes;
	}
}
