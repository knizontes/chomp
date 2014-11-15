package main_pack.data.avl.string_key;

import java.io.Serializable;
import java.util.ArrayList;

import main_pack.data.avl.AVLTree;

/**
 * 
 * Implementazione dell'AVL tree utile per il lookup performante delle risorse del sistema. Differisce dalla generica
 * implementazione di un AVL poiche' specializzata per il trattamento di dati quali stringhe.
 *
 */

public class AVLStringKeyTree  implements Serializable{
	
	private ArrayList<AVLStringKeyNode> list;
	private AVLStringKeyNode search_root;
	private int cur;
	private final int LEFT = 0;
	private final int RIGHT = 1;
	private final int ADDITION = 0;
	private final int REMOVING = 1;
	private int nullcounter;
	private final int NULLTHRESOLD=3;
	public static final short EQUAL=0;
	public static final short GREATER=1;
	public static final short LOWER=2;
	
//	private HSNode hsnode;
	
	public AVLStringKeyTree() {
		cur=0;
		search_root=null;
		list= new ArrayList<AVLStringKeyNode>();
		nullcounter=0;

	}
	
	/*
	 * Char comparing function
	 */
	private short compareChar(char c1, char c2){
		if (c1==c2)
			return EQUAL;
		if (c1>c2)
			return GREATER;
		return LOWER;
	}
	
	/*
	 * String comparing function
	 * Note: the Strings must be of the same size!
	 */
	public short compareStrings(String s1, String s2){
		short retval;
		if (s2.length()>s1.length()){
			for (int i=0; i<s1.length(); ++i){
				if ((retval=compareChar(s1.charAt(i), s2.charAt(i)))==EQUAL)
					continue;
//				if (retval==GREATER)
//					System.out.println(s1+" GREATER "+s2+"!\n");
//				if (retval==LOWER)
//					System.out.println(s1+" LOWER !"+s2+"\n");
				return retval;
			}
//			System.out.println(s1+" LOWER "+s2+"!\n");
			return LOWER;
		}else{
			for (int i=0; i<s2.length(); ++i){
				if ((retval=compareChar(s1.charAt(i), s2.charAt(i)))==EQUAL)
					continue;				
//				if (retval==GREATER)
//					System.out.println(s1+" GREATER "+s2+"!\n");
//				if (retval==LOWER)
//					System.out.println(s1+" LOWER "+s2+"!\n");

				return retval;
			}
		}
//		System.out.println(s1+" EQUAL "+s2+"!\n");
			
		return EQUAL; 
	}
	
	public Boolean greaterString(String s1, String s2){
		if (compareStrings(s1, s2)==GREATER)
			return true;
		return false;
	}
	
	public Boolean nonLowerString(String s1, String s2){
		if (compareStrings(s1, s2)==LOWER)
			return false;
		return true;
	}
	
	public Boolean lowerString(String s1, String s2){
		if (compareStrings(s1, s2)==LOWER)
			return true;
		return false;
	}
	
	public Boolean nonGreaterString(String s1, String s2){
		if (compareStrings(s1, s2)==GREATER)
			return false;
		return true;
	}
	
	public Boolean equalString(String s1, String s2){
		if (compareStrings(s1, s2)==EQUAL)
			return true;
		return false;
	}
	
	public Boolean nonEqualString(String s1, String s2){
		if (compareStrings(s1, s2)==EQUAL)
			return false;
		return true;
	}
	
	public AVLStringKeyNode search(AVLStringKeyNode nd,AVLStringKeyNode from_nd){
		String id = nd.getNode_id();
		while (true){
			if (from_nd==null) 
				return null;
			if (equalString(id, from_nd.getNode_id()))
				return from_nd;
			if (greaterString(id,from_nd.getNode_id()))
				from_nd=from_nd.getS_right_son();
			else
				from_nd=from_nd.getS_left_son();
		}
	}
	
	public AVLStringKeyNode search(String nodeId){
//		int id = nd.getNode_id();
		AVLStringKeyNode from_nd= search_root;
		while (true){
			if (from_nd==null) 
				return null;
			short comparison=compareStrings(nodeId,from_nd.getNode_id());
			if (comparison==EQUAL)
				return from_nd;
			if (comparison==GREATER)
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
	
	public AVLStringKeyNode getNodeX (int index){
		if (index>=list.size()){
			return null;
		}
		if (list.get(index)==null)
			return null;
		return list.get(index);
	}
	
	private int max_node_h(AVLStringKeyNode nd){
		if (nd.getS_left_h()>nd.getS_right_h())
			return LEFT;
		return RIGHT;
	}
	
	private void set_root(AVLStringKeyNode nd){
		this.search_root=nd;
	}
	
	private int set_node_father_h(AVLStringKeyNode nd){
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
	
	private void balance_node(AVLStringKeyNode nd, int flag){
		AVLStringKeyNode temp;
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
	
	private int check_balance_factor(AVLStringKeyNode nd){
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
	
	private void check_balance(AVLStringKeyNode node, int flag){
		int cbf;
		while (true){
//			System.out.println("[AVLSKT]ooops");
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
		AVLStringKeyNode ln;
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
	
	/**
	 * Add a node in the avl tree
	 * @param nd the node to add
	 * @return 1 if the node is already present, 2 if the new node is the first node in the tree, 0 if the node is correctly 
	 * added in the avl tree, -1 if something wrong occurred
	 */
	
	public int add_node(AVLStringKeyNode nd){
//		System.out.println("Adding node "+nd.getNode_id()+" to the list of node:"+hsnode.getNode_id());
		AVLStringKeyNode present_node,node;
		Boolean b;
//		int id= nd.getNode_id();
//		int bigger;
		if (search(nd,search_root)!=null){
//			printTree();
			/*1 is returned if the node is already present (idempotency)*/
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
//			System.out.println("[AVLSKT DEB] nd:"+node.getNode_id()+" pnd:"+present_node.getNode_id()+" sr:"+search_root.getNode_id());
			short comparison= compareStrings(node.getNode_id(),present_node.getNode_id());
			if (comparison==LOWER){
				if (present_node.getS_left_son()==null){
					present_node.setS_left_son(node);
					node.setS_father(present_node);
					check_balance(node,ADDITION);
					/* 0 is returned if node insertion succeed*/
					return 0;				
				}
				else present_node = present_node.getS_left_son();
			}
			if (comparison==GREATER){
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
		++ cur;
	}
	
	
	public AVLStringKeyNode getSearch_root() {
		return search_root;
	}
	
	/**
	 * remove_node() remove the argument node if any and returns 1,
	 * if no nodes with argument address is matched returns 0;
	 */
	public int remove_node(AVLStringKeyNode nd){
//		System.out.println("[AVLSKT]removing node "+nd.getNode_id());
		AVLStringKeyNode ln= search(nd,search_root);
		if (ln==null){
//			printTree();
			return 0;
		}
		/*if (nd.getNode_id().equals(search_root.getNode_id())){
			list.set(ln.getPosition(), null);
			++nullcounter;
			nullCollector();
			return 1;
		}*/
			
		AVLStringKeyNode pred;
		/*No left subtree from leaving node*/
		if (ln.getS_left_h()==0){
			/*No left nor right subtree from leaving node*/
			if (ln.getS_right_h()==0){
				AVLStringKeyNode father = ln.getS_father();
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
				else{
					printTree();
					search_root=null;
				}
				
				/*removing node from the list*/
				 list.set(ln.getPosition(), null);
				 ++nullcounter;
				 nullCollector();
				 
				 if (father!=null){
					 check_balance(father,REMOVING);
				 }
				 
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
		pred= getPredecessor(ln);
		
		ln.setNode(pred);
		ln.setNode_id(pred.getNode_id());

//		kkk
		if (!(pred.getNode_id()==ln.getS_left_son().getNode_id()))
				pred.getS_father().setS_right_son(pred.getS_left_son());
		else
			ln.setS_left_son(pred.getS_left_son());

		if (ln.getS_left_son()==null){
			ln.setS_left_h(0);
			check_balance(ln,REMOVING);//verifica
		}
		

		else if (pred.getS_left_son()!=null){
			pred.getS_left_son().setS_father(pred.getS_father());
			check_balance(pred.getS_left_son(),REMOVING);
		}
		
		else{
			pred.getS_father().setS_right_h(0);
			check_balance(pred.getS_father(),REMOVING);
		}
		list.set(pred.getPosition(), null);
		++nullcounter;
		nullCollector();

//		printTree();
		return 1;
		
	}
	
	public int size(){
		return list.size();
	}
	
	public int size_effective(){
		return (list.size()-nullcounter);
	}
	
	

	
	public AVLStringKeyNode getPredecessor (AVLStringKeyNode ln){
//		System.out.println("Node:"+ln.getNode_id()+" height:"+ln.getS_left_h()+","+ln.getS_right_h());
		AVLStringKeyNode retval= ln.getS_left_son();
		AVLStringKeyNode rson=retval.getS_right_son();
//		System.out.println("Ciao");
		while (rson!=null){
			retval= rson;
			rson=rson.getS_right_son();
		}
		
		return retval;
	}
	
	private void nullCollector(){
		if (nullcounter>NULLTHRESOLD){
//			System.out.println("----------------------NULL COLLECTOR--------------------------");
//			int offset=0;
			int i;
			for ( i=0; (i<list.size()) && (nullcounter>=0); ++i){
				while (list.get(i)==null){
//					++offset;
					list.remove(i);
//					--nullcounter;
					if (i>=list.size()){
//						cur-=offset;
						cur=i;
						nullcounter=0;
						return;
					}
				}
//				System.out.println("removed "+offset+" null items");
//				System.out.print("decreased position of node "+list.get(i).getNode_id()+ "from position:"+list.get(i).getPosition());
				list.get(i).setPosition(i);
//				System.out.println(" to position:"+list.get(i).getPosition());
			}
//			cur-=offset;
			cur=i;
			nullcounter=0;
		}
		return;
	}
	
	public AVLStringKeyNode keySuccessor(String key){
		AVLStringKeyNode successor=null;
		AVLStringKeyNode from_node=search_root;
		while (true){
			short comparison = compareStrings(key, from_node.getNode_id());
			if (comparison==EQUAL)
				return from_node;
			if (comparison==GREATER){
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
	
	public int getNodesNum(){
		return list.size();
	}
	

	
	

}












