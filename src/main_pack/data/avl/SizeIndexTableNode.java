package main_pack.data.avl;

import java.util.ArrayList;

import main_pack.data.IndexTable;




public class SizeIndexTableNode extends AVLNode{
	
	private ArrayList<Integer> arrayIndexList;
	
	public SizeIndexTableNode(IndexTable it, int index) {
		super(it.size());
		arrayIndexList = new ArrayList<Integer>();
		arrayIndexList.add(new Integer(index));
	}
	
	public void addIndex(int index){
		arrayIndexList.add(new Integer(index));
	}
	
	
	public ArrayList<Integer> getArrayIndexList(){
		return arrayIndexList;
	}

	@Override
	public void setNode(AVLNode nd) {
		setNode_id(nd.getNode_id());
		arrayIndexList=((SizeIndexTableNode)nd).getArrayIndexList();
	}

}
