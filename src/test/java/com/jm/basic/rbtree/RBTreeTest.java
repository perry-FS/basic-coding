package com.jm.basic.rbtree;

public class RBTreeTest {
	public static void main(String[] args) {
		RBTree<Integer> rbTree = new RBTree<>();
		for (int i = 1; i<=15; i++) {
			rbTree.insert(i);
		}

		rbTree.printTree();
		System.out.println("tree high:" + rbTree.getTreeHigh());

		for (int i = 1; i <= 15; i++) {
			rbTree.delete(i);
			System.out.println("delete----------" + i);
			rbTree.printTree();
		}
	}
}
