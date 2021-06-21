import java.util.ArrayList;

public abstract class Node {
	int degree;
	ArrayList<Integer> keys; // 노드마다 가지고 있을 키가 보관될 변수 
	int nlSplitKey = -1; // NonLeafNode에서 split 될 때 사용하는 값 
	
	abstract Node[] insert(int key, int value);
	abstract int delete(int key, Node parentNode, Node tmpRoot);
	abstract void singleKeySearch(int findKey);
	abstract void rangedSearch(int startKey, int endKey);
	
	abstract Node split();
	abstract int merge(Node leftSibling, Node rightSibling, Node parentNode);
	
	abstract void print();
	abstract ArrayList<Node> getNode(ArrayList<Node> nodes);
}
