package main_pack.data.avl;

import java.util.ArrayList;


/**
 * 
 * Abstract Class. Generica implementazione dell'AVL tree.
 *
 */
public class AVLTree {
	
	private ArrayList<AVLNode> list;
	private AVLNode search_root;
	private int cur;
	private final int LEFT = 0;
	private final int RIGHT = 1;
	private final int ADDITION = 0;
	private final int REMOVING = 1;
	private int nullcounter;
	private final int NULLTHRESOLD=3;
//	private HSNode hsnode;
	
	public AVLTree(/*HSNode hsnode*/) {
		cur=0;
		search_root=null;
		list= new ArrayList<AVLNode>();
		nullcounter=0;
//		this.hsnode=hsnode;
	}
	
	public AVLNode search(AVLNode nd,AVLNode from_nd){
		int id = nd.getNode_id();
		while (true){
			if (from_nd==null) 
				return null;
			if (id==from_nd.getNode_id())
				return from_nd;
			if (id>from_nd.getNode_id())
				from_nd=from_nd.getS_right_son();
			else
				from_nd=from_nd.getS_left_son();
		}
	}
	
	public AVLNode search(int nodeId){
//		int id = nd.getNode_id();
		AVLNode from_nd= search_root;
		while (true){
			if (from_nd==null) 
				return null;
			if (nodeId==from_nd.getNode_id())
				return from_nd;
			if (nodeId>from_nd.getNode_id())
				from_nd=from_nd.getS_right_son();
			else
				from_nd=from_nd.getS_left_son();
		}
	}
	
	
	/*public HSNode search_node(HSNode nd,AVLNode from_nd){
		int id = nd.getNode_id();
		while (true){
			if (from_nd==null) 
				return null;
			if (id==from_nd.getNode_id())
				return from_nd.getNode();
			if (id>from_nd.getNode_id())
				from_nd=from_nd.getS_right_son();
			else
				from_nd=from_nd.getS_left_son();
		}
	}*/
	
	public AVLNode getNode (int index){
		if (index>=list.size()){
			return null;
		}
		if (list.get(index)==null)
			return null;
		return list.get(index);
	}
	
	private int max_node_h(AVLNode nd){
		if (nd.getS_left_h()>nd.getS_right_h())
			return LEFT;
		return RIGHT;
	}
	
	private void set_root(AVLNode nd){
		this.search_root=nd;
	}
	
	private int set_node_father_h(AVLNode nd){
		int l_max_h, r_max_h;
		if (nd.getS_left_son()==null){
			nd.setS_left_h(0);
			if(nd.getS_right_son()==null){
				nd.setS_right_h(0);
				return 0;
			}
			else {
				if (nd.getS_right_son().getS_left_h()>nd.getS_right_son().getS_right_h())
					r_max_h=nd.getS_right_son().getS_left_h();
				else r_max_h=nd.getS_right_son().getS_right_h();
				nd.setS_right_h(r_max_h+1);
				return r_max_h;
			}
		}
		
		if (nd.getS_right_son()==null){
			nd.setS_right_h(0);
			if (nd.getS_left_son().getS_left_h()>nd.getS_left_son().getS_right_h())
				l_max_h=nd.getS_left_son().getS_left_h();
			else l_max_h=nd.getS_left_son().getS_right_h();
			nd.setS_left_h(l_max_h+1);
			return l_max_h;
		}
		
		if (nd.getS_left_son().getS_left_h()>nd.getS_left_son().getS_right_h())
			l_max_h=nd.getS_left_son().getS_left_h();
		else l_max_h=nd.getS_left_son().getS_right_h();
		
		if (nd.getS_right_son().getS_left_h()>nd.getS_right_son().getS_right_h())
			r_max_h=nd.getS_right_son().getS_left_h();
		else r_max_h=nd.getS_right_son().getS_right_h();
		
		nd.setS_left_h(l_max_h+1);
		nd.setS_right_h(r_max_h+1);
		
		if (l_max_h>r_max_h) return l_max_h;
		return r_max_h;
		
	}
	
	private void balance_node(AVLNode nd, int flag){
		AVLNode temp;
//		System.out.println("balancing "+nd.getNode_id()+" node");
		if (flag==LEFT){
			/*left-left condition, right rotation*/
			if (max_node_h(nd)==LEFT){
				temp=nd.getS_left_son();
				
//				System.out.println("Left-left condition, right rotation...");
				/*the node to balance is not root*/
				if (nd.getS_father()!=null){
					/*the node to balance is a left son*/
					if (nd.getS_father().getS_left_son()==nd)
						nd.getS_father().setS_left_son(temp);
					/*the node to balance is a right son*/
					else nd.getS_father().setS_right_son(temp);
				}
				
				temp.setS_father(nd.getS_father());
				
				nd.setS_father(temp);
				nd.setS_left_son(temp.getS_right_son());
				
				if (nd.getS_left_son()!=null)
					nd.getS_left_son().setS_father(nd);
				
				temp.setS_right_son(nd);
				
				if (temp.getS_father()==null)
					set_root(temp);
				
				set_node_father_h(nd);
				set_node_father_h(temp);
				
			}
			/*left-right condition, double right rotation*/
			else {
				temp=nd.getS_left_son().getS_right_son();
//				System.out.println("left-right condition, double right rotation...");
				if (nd.getS_father()!=null){
					if (nd.getS_father().getS_left_son()==nd)
						nd.getS_father().setS_left_son(temp);
					else nd.getS_father().setS_right_son(temp);
				}
				
				temp.setS_father(nd.getS_father());
				nd.setS_father(temp);
				nd.getS_left_son().setS_father(temp);
				nd.getS_left_son().setS_right_son(temp.getS_left_son());
				
				if (nd.getS_left_son().getS_right_son()!=null)
					nd.getS_left_son().getS_right_son().setS_father(nd.getS_left_son());
				
				temp.setS_left_son(nd.getS_left_son());
				nd.setS_left_son(temp.getS_right_son());
				
				if (nd.getS_left_son()!=null)
					nd.getS_left_son().setS_father(nd);
				
				temp.setS_right_son(nd);
				
				if (temp.getS_father()==null)
					set_root(temp);
				
				set_node_father_h(nd);
				set_node_father_h(temp.getS_left_son());
				set_node_father_h(temp);
				
			}
			
		}
		/*Right flag*/
		else{
			/*right-right condition, left rotation*/
			if (max_node_h(nd.getS_right_son())==RIGHT){
				temp = nd.getS_right_son();
//				System.out.println("right-right condition, left rotation...");
				
				if (nd.getS_father()!=null){
					if (nd.getS_father().getS_left_son()==nd)
						nd.getS_father().setS_left_son(temp);
					else nd.getS_father().setS_right_son(temp);
				}
				
				temp.setS_father(nd.getS_father());
				nd.setS_father(temp);
				nd.setS_right_son(temp.getS_left_son());
				
				if (nd.getS_right_son()!=null)
					nd.getS_right_son().setS_father(nd);
				
				temp.setS_left_son(nd);
				
				if (temp.getS_father()==null) 
					set_root(temp);
				
				set_node_father_h(nd);
				set_node_father_h(temp);
			}
			else {
				/*Right-left condition*/
				temp= nd.getS_right_son().getS_left_son();
//				System.out.println("right-left condition, double left rotation...");
				if (nd.getS_father()!=null){
					if (nd.getS_father().getS_left_son()==nd)
						nd.getS_father().setS_left_son(temp);
					else nd.getS_father().setS_right_son(temp);
				}
				
				temp.setS_father(nd.getS_father());
				nd.setS_father(temp);
				nd.getS_right_son().setS_father(temp);
				nd.getS_right_son().setS_left_son(temp.getS_right_son());
				
				if (nd.getS_right_son().getS_left_son()!=null)
					nd.getS_right_son().getS_left_son().setS_father(nd.getS_right_son());
				
				temp.setS_right_son(nd.getS_right_son());
				nd.setS_right_son(temp.getS_left_son());
				
				if (nd.getS_right_son()!=null)
					nd.getS_right_son().setS_father(nd);
				
				temp.setS_left_son(nd);
				
				if (temp.getS_father()==null)
					set_root(temp);
				
				set_node_father_h(nd);
				set_node_father_h(temp.getS_right_son());
				set_node_father_h(temp);
				
			}
		}
		
	}
	
	private int check_balance_factor(AVLNode nd){
		int max_h,balancing_factor;
		
		if (nd.getS_left_h()>nd.getS_right_h()){
			max_h=nd.getS_left_h();
			balancing_factor= nd.getS_left_h()- nd.getS_right_h();
			if (balancing_factor >=2){
				balance_node(nd, LEFT);
				/*The function returns 2 if a balance occurred*/
				return 2;
			}
		}
		
		else {
			max_h=nd.getS_right_h();
			balancing_factor= nd.getS_right_h()-nd.getS_left_h();
			if (balancing_factor>=2){
				balance_node(nd, RIGHT);
				/*The function returns 2 if a balance occurred*/
				return 2;
			}
		}
		
		/*The function returns 1 if the node to check is the root*/
		if (this.search_root==nd)
			return 1;								
		
		if (nd.getS_father().getS_left_son()==nd)
			nd.getS_father().setS_left_h(max_h+1);
		else if (nd.getS_father().getS_right_son()==nd)
			nd.getS_father().setS_right_h(max_h+1);
		/*The function returns -1 if an error occurred */
		else return -1;														
		
		/*0 is returned if no balance occurred*/
		return 0;
//		return check_balance_factor(nd.getS_father());
	}
	
	private void check_balance(AVLNode node, int flag){
		int cbf;
		while (true){
			cbf=check_balance_factor(node);
			if ((cbf==1)||(cbf<0))
				return;
			if((flag==ADDITION)&&(cbf!=0))
				return;
			node=node.getS_father();
		}
		
	}
	
	
	
	public void printTree(){
		System.out.println("Printing list tree:");
		AVLNode ln;
		for (int i=0; i<list.size();++i){
			if ((ln=list.get(i))!=null){
				System.out.print("nd:"+ln.getNode_id());
				if (ln.getS_father()!=null)
					System.out.print("\tfather:"+ln.getS_father().getNode_id());
				else 
					System.out.print("\tfather:null");
				System.out.print("\tlh:"+ln.getS_left_h());
				System.out.print("\trh:"+ln.getS_right_h());
				if (ln.getS_left_son()!=null)
					System.out.print("\tl son:"+ln.getS_left_son().getNode_id());
				else 
					System.out.print("\tl son:null");
				if (ln.getS_right_son()!=null)
					System.out.print("\tr son:"+ln.getS_right_son().getNode_id());
				else
					System.out.print("\tr son:null");
				System.out.println("\tpos:"+ln.getPosition());
			}
		}
		
	}
	
	
	public int add_node(AVLNode nd){
//		System.out.println("Adding node "+nd.getNode_id()+" to the list of node:"+hsnode.getNode_id());
		AVLNode present_node,node;
		Boolean b;
//		int id= nd.getNode_id();
//		int bigger;
		if (search(nd,search_root)!=null){
//			printTree();
			/*1 is returned if the node is already present (idempotenza)*/
			return 1;						
		}
		list.add(nd);
		node = list.get(cur);
//		node.setNode_id(id);
		node.setPosition(cur);
		increase_cur();
		if (search_root==null){
			search_root=node;
//			printTree();
			/*2 is returned if the node is the first node in the tree*/
			return 2;						
		}
		
		present_node = this.search_root;
		b=true;
		while (b){
			if (node.getNode_id()<present_node.getNode_id()){
				if (present_node.getS_left_son()==null){
					present_node.setS_left_son(node);
					node.setS_father(present_node);
					check_balance(node,ADDITION);
					/* 0 is returned if node insertion succeed*/
					return 0;				
				}
				else present_node = present_node.getS_left_son();
			}
			if (node.getNode_id()>present_node.getNode_id()){
				if (present_node.getS_right_son()==null){
					present_node.setS_right_son(node);
					node.setS_father(present_node);
					check_balance(node,ADDITION);
//					printTree();
					return 0;				// 0 is returned if node insertion succeed
				}
				else present_node = present_node.getS_right_son();
			}
		}
//		printTree();
		return -1;
	}
	
	private void increase_cur(){
		++ this.cur;
	}
	
	
	public AVLNode getSearch_root() {
		return search_root;
	}
	
	/*
	 * remove_file() remove the argument node if any and returns 1,
	 * if no nodes with argument address is matched returns 0;
	 */
	public int remove_node(AVLNode nd){
//		System.out.println("removing node "+nd.getNode_id()+" to the list of node "+hsnode.getNode_id());
		AVLNode ln= search(nd,search_root);
		if (ln==null){
			return 0;
		}
		
		AVLNode pred;
		/*No left subtree from leaving node*/
		if (ln.getS_left_h()==0){
			
			/*No left nor right subtree from leaving node*/
			if (ln.getS_right_h()==0){
				AVLNode father = ln.getS_father();
				/*the leaving node is not the root*/
				if (father!=null){
					/*the leaving node is a left son*/
					if(ln.getS_father().getS_left_son()==ln){
						ln.getS_father().setS_left_son(null);
						ln.getS_father().setS_left_h(0);
					}
					/*the leaving node is a right son*/
					else{
						ln.getS_father().setS_right_son(null);
						ln.getS_father().setS_right_h(0);
					}
				}
				
				/*the leaving node is the root*/
				else
					search_root=null;
				
				/*removing node from the list*/
				 list.set(ln.getPosition(), null);
				 ++nullcounter;
				 nullCollector();
				 if (father!=null)
					 check_balance(father,REMOVING);
//				 printTree();
				 return 1;
			}
			
			/*the node has no left subtree but a leaf right son*/
			
			/*the node is the root*/
			if (ln.getS_father()==null){
				search_root=ln.getS_right_son();
				ln.getS_right_son().setS_father(null);
				list.set(ln.getPosition(), null);
				++nullcounter;
				nullCollector();
//				printTree();
				return 1;
			}
			
			ln.getS_right_son().setS_father(ln.getS_father());
			
			if (ln.getS_father()!=null){
				if((ln.getS_father().getS_left_son()==ln))
					ln.getS_father().setS_left_son(ln.getS_right_son());
				else
					ln.getS_father().setS_right_son(ln.getS_right_son());
				check_balance(ln.getS_right_son(),REMOVING);
			}
			
			list.set(ln.getPosition(),null);
			++nullcounter;
			nullCollector();
//			printTree();
			return 1;
		}
		
		/*No right subtree from leaving node*/
		if (ln.getS_right_son()==null){
			if (ln.getS_father()==null){
				search_root=ln.getS_left_son();
				ln.getS_left_son().setS_father(null);
				list.set(ln.getPosition(),null);
				++nullcounter;
				nullCollector();
//				printTree();
				return 1;
			}
			
			ln.getS_left_son().setS_father(ln.getS_father());
			
			if (ln.getS_father()!=null){
				if (ln.getS_father().getS_left_son()==ln)
					ln.getS_father().setS_left_son(ln.getS_left_son());
				else
					ln.getS_father().setS_right_son(ln.getS_left_son());
				check_balance(ln.getS_left_son(),REMOVING);
			}
			
			list.set(ln.getPosition(),null);
			
			++nullcounter;
			nullCollector();
//			printTree();
			return 1;
		}
		
		pred= predecessor(ln);
		
		/*replacing ln with predecessor*/
		ln.setNode(pred);
		
		if (!(pred.getNode_id()==ln.getS_left_son().getNode_id()))
				pred.getS_father().setS_right_son(pred.getS_left_son());
		else
			ln.setS_left_son(pred.getS_left_son());
		
		if (ln.getS_left_son()==null){
			ln.setS_left_h(0);
			check_balance(ln,REMOVING);
		}
		
		else if (pred.getS_left_son()!=null){
			pred.getS_left_son().setS_father(pred.getS_father());
			check_balance(pred.getS_left_son(),REMOVING);
		}
		
		else{
			pred.getS_father().setS_right_h(0);
			pred.getS_father().setS_right_son(null);
			check_balance(pred.getS_father(),REMOVING);
		}
		
		list.set(pred.getPosition(), null);
		++nullcounter;
		nullCollector();
//		printTree();
		return 1;
		
	}
	
	/**
	 * 
	 * @return the number of nodes in the avl
	 */
	
	public int size(){
		return list.size();
	}
	
	public int size_effective(){
		return (list.size()-nullcounter);
	}
	
	

	
	public AVLNode predecessor (AVLNode ln){
//		System.out.println("Node:"+ln.getNode_id()+" height:"+ln.getS_left_h()+","+ln.getS_right_h());
		AVLNode retval= ln.getS_left_son();
		AVLNode rson=retval.getS_right_son();
//		System.out.println("Ciao");
		while (rson!=null){
			retval= rson;
			rson=rson.getS_right_son();
		}
		
		return retval;
	}
	
	public AVLNode getMinNode(){
		AVLNode fromNode=search_root;
		AVLNode tmpNode;
		for(;;){
			if ((tmpNode=fromNode.getS_left_son())==null)
				return fromNode;
			fromNode=tmpNode;
		}
	}
	
	public AVLNode getMaxNode(){
		AVLNode fromNode=search_root;
		AVLNode tmpNode;
		for(;;){
			if ((tmpNode=fromNode.getS_right_son())==null)
				return fromNode;
			fromNode=tmpNode;
		}
	}
	
	public AVLNode getMaxNodePredecessor(){
		AVLNode from_node=search_root;
		AVLNode tmpNode=null;
		for(;;){
			if ((tmpNode=from_node.getS_right_son())==null){
				break;
			}
			from_node=tmpNode;
		}
		return keyStrictPredecessor(from_node.getNode_id());
	}
	
	public AVLNode keyStrictPredecessor (int nd_id){
		AVLNode predecessor =null;
		AVLNode tmpNode;
		AVLNode from_node=search_root;
		for(;;){
			if (nd_id<=from_node.getNode_id()){
				if ((tmpNode=from_node.getS_left_son())==null)
					return predecessor;
				from_node=tmpNode;
			}
			else {
				predecessor=from_node;
				if ((tmpNode=from_node.getS_right_son())==null)
					return predecessor;
				from_node=tmpNode;
			}
		}
	}
	
	private void nullCollector(){
		if (nullcounter>NULLTHRESOLD){
//			System.out.println("----------------------NULL COLLECTOR--------------------------");
			int offset=0;
			for (int i=0; (i<list.size()) && (nullcounter>=0); ++i){
				while (list.get(i)==null){
					++offset;
					list.remove(i);
					--nullcounter;
					if (i>=list.size()){
						cur-=offset;
						return;
					}
				}
//				System.out.println("removed "+offset+" null items");
//				System.out.print("decreased position of node "+list.get(i).getNode_id()+ "from position:"+list.get(i).getPosition());
				list.get(i).decreasePosition(offset);
//				System.out.println(" to position:"+list.get(i).getPosition());
			}
			cur-=offset;
		}
		return;
	}
	
	public AVLNode keySuccessor(int key){
		AVLNode successor=null;
		AVLNode from_node=search_root;
		for(;;){
			if (key==from_node.getNode_id())
				return from_node;
			if (key>from_node.getNode_id()){
				if (from_node.getS_right_son()==null)
					return successor;
				from_node=from_node.getS_right_son();
			}
			else{
				successor=from_node;
				if(from_node.getS_left_son()==null)
					return successor;
				from_node=from_node.getS_left_son();
			}
		}
		
	}
	
	public static void main(String[] args) {
		AVLTree avl = new AVLTree();
		int n = 93;
		AVLNode nd=null;
		for (int i=0; i<15; ++i){
			AVLNode tnd=new AVLNode((i*53)%n) {
				
				@Override
				public void setNode(AVLNode nd) {
					setNode_id(nd.getNode_id());
				}
			};
			avl.add_node(tnd);
			if (i==14)
				nd=tnd;
				
		}
		avl.printTree();
		System.out.println("\n\nRemoving node:"+nd.getNode_id());
		avl.remove_node(nd);
		avl.printTree();
	}
	
//	funcTimes[ADDBROTHERLIST]+= (System.nanoTime()) - t;

}












