package main_pack.data.avl;

import main_pack.data.IndexTable;

public class SizeIndexTableTree extends AVLTree{

	public int add_node (IndexTable it, int index){
		SizeIndexTableNode s_it_node_temp;
		int retval=3;
		if ((s_it_node_temp=(SizeIndexTableNode) search(it.size()))==null)
			retval = super.add_node(new SizeIndexTableNode(it, index));
		else
			s_it_node_temp.addIndex(index);
		return retval;
	}
	
}
