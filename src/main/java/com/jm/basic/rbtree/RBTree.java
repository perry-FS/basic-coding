package com.jm.basic.rbtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class RBTree<T extends Comparable<T>> {

	private static class Node<T extends Comparable<T>> {
		private T t;
		private boolean isRed = true;
		private Node<T> parent;
		private Node<T> left;
		private Node<T> right;
		private int high;

		public Node(T t) {
			this.t = t;
		}

		private boolean isLeftNode() {
			return t.compareTo(parent.t) < 0 ? true : false;
		}

		private void printNode() {
			String color = isRed ? "R" : "B";
			T parentV = parent != null ? parent.t : null;
			T leftV = left != null ? left.t : null;
			T rightV = right != null ? right.t : null;
			System.out.println(high + ":" + color + ":" + t + "---P:" + parentV + "---L:" + leftV + "---R:" + rightV);
		}
	}

	private Node<T> root = null;

	private int totalHigh = 0;

	public void insert(T insertValue) {
		if (root == null) {
			root = new Node<>(insertValue);
			root.isRed = false;
			root.high = 1;
			if (root.high > totalHigh)
				totalHigh = root.high;
			return;
		}
		// 找到插入节点的父节点
		Node<T> parent = findNode(root, insertValue);
		int v = insertValue.compareTo(parent.t);
		if (v == 0)
			return;
		Node<T> insertNode = new Node<>(insertValue);
		if (v > 0)
			parent.right = insertNode;
		else
			parent.left = insertNode;

		insertNode.parent = parent;
		insertNode.high = parent.high + 1;
		balanceInsertion(parent, insertNode);
		if (insertNode.high > totalHigh)
			totalHigh = insertNode.high;
	}

	public void delete(T deleteValue) {
		if (root == null)
			return;
		// 查找删除节点
		Node<T> deleteNode = findNode(root, deleteValue);
		int v = deleteValue.compareTo(deleteNode.t);
		if (v != 0)
			return;

		// 没有子节点
		if (deleteNode.left == null && deleteNode.right == null) {
			if (deleteNode == root) {
				root = null;
				return;
			} else {
				Node<T> parentNode = deleteNode.parent;
				boolean left = deleteNode.isLeftNode();
				if (left)
					parentNode.left = null;
				else
					parentNode.right = null;

				balanceDeletion(parentNode, deleteNode);
			}
		}
		// 只有右子节点
		if (deleteNode.left == null && deleteNode.right != null) {
			Node<T> right = deleteNode.right;
			if (deleteNode == root) {
				deleteNode.right = null;
				right.parent = null;
				root = right;
			} else {
				Node<T> parentNode = deleteNode.parent;
				right.isRed = false;
				boolean isLeft = deleteNode.isLeftNode();
				if (isLeft) {
					parentNode.left = right;
				} else {
					parentNode.right = right;
				}
				right.parent = parentNode;
				deleteNode.parent = null;
				deleteNode.right = null;
			}
			decreaseHigh(Arrays.asList(right));
		}
		// 只有左子节点
		if (deleteNode.left != null && deleteNode.right == null) {
			Node<T> left = deleteNode.left;
			if (deleteNode == root) {
				deleteNode.left = null;
				left.parent = null;
				root = left;
			} else {
				Node<T> parentNode = deleteNode.parent;
				left.isRed = false;
				boolean isLeft = deleteNode.isLeftNode();
				if (isLeft)
					parentNode.left = left;
				else
					parentNode.right = left;

				left.parent = parentNode;
				deleteNode.parent = null;
				deleteNode.right = null;
			}
			decreaseHigh(Arrays.asList(left));
		}
		if (deleteNode.left != null && deleteNode.right != null) {
			// 找到后继节点
			Node<T> nextNode = findNext(deleteNode.right);
			T tmp = nextNode.t;
			delete(tmp);
			deleteNode.t = tmp;
		}
	}

	/**
	 * 查找值为query的节点或者query作为插入值的父节点
	 * 
	 * @param entry
	 * @param query
	 * @return
	 */
	private Node<T> findNode(Node<T> entry, T query) {
		int v = query.compareTo(entry.t);
		if (v == 1) {
			if (entry.right == null)
				return entry;
			else
				return findNode(entry.right, query);
		} else if (v == -1) {
			if (entry.left == null)
				return entry;
			else
				return findNode(entry.left, query);
		} else {
			return entry;
		}
	}

	/**
	 * 找到entry节点的最左子节点
	 * 
	 * @param entry
	 * @return
	 */
	private Node<T> findNext(Node<T> entry) {
		while (entry.left != null) {
			entry = entry.left;
		}
		return entry;
	}

	/**
	 * 自平衡
	 */
	private void balanceInsertion(Node<T> parentNode, Node<T> insertNode) {
		if (!parentNode.isRed)
			return;
		Node<T> grandpaNode = parentNode.parent;
		boolean isParentLeft = parentNode.isLeftNode();
		boolean isChildLeft = insertNode.isLeftNode();
		Node<T> uncleNode = isParentLeft ? grandpaNode.right : grandpaNode.left;

		if (uncleNode != null && uncleNode.isRed) {
			parentNode.isRed = false;
			uncleNode.isRed = false;
			if (grandpaNode != root) {
				grandpaNode.isRed = true;
				balanceInsertion(grandpaNode.parent, grandpaNode);
			}
			return;
		}

		if (isParentLeft) {
			if (isChildLeft) {
				rotateToRight(grandpaNode, this::changeColor2);
			} else {
				rotateToLeft(parentNode, this::changeColor0);
				rotateToRight(grandpaNode, this::changeColor2);
			}
		} else {
			if (isChildLeft) {
				rotateToRight(parentNode, this::changeColor0);
				rotateToLeft(grandpaNode, this::changeColor2);
			} else {
				rotateToLeft(grandpaNode, this::changeColor2);
			}
		}
	}

	private void balanceDeletion(Node<T> parentNode, Node<T> deleteNode) {
		if (deleteNode.isRed)
			return;
		if (deleteNode == root)
			return;
		boolean isLeft = deleteNode.isLeftNode();
		if (isLeft) {
			Node<T> brotherNode = parentNode.right;
			if (brotherNode.isRed) {
				rotateToLeft(parentNode, this::changeColor2);
				balanceDeletion(parentNode, deleteNode);
			} else {
				Node<T> leftNephew = brotherNode.left;
				Node<T> rightNephew = brotherNode.right;
				if ((leftNephew == null && rightNephew == null)
						|| (leftNephew != null && rightNephew != null && !leftNephew.isRed && !rightNephew.isRed)) {
					brotherNode.isRed = true;
					if (parentNode.isRed)
						parentNode.isRed = false;
					else
						balanceDeletion(parentNode.parent, parentNode);
					return;
				}
				if (rightNephew != null && rightNephew.isRed) {
					rotateToLeft(parentNode, this::changeColor3);
					return;
				}
				if (leftNephew != null && leftNephew.isRed) {
					rotateToRight(brotherNode, this::changeColor2);
					rotateToLeft(parentNode, this::changeColor3);
					return;
				}
			}
		} else {
			Node<T> brotherNode = parentNode.left;
			if (brotherNode.isRed) {
				rotateToRight(parentNode, this::changeColor2);
				balanceDeletion(parentNode, deleteNode);
			} else {
				Node<T> leftNephew = brotherNode.left;
				Node<T> rightNephew = brotherNode.right;
				if ((leftNephew == null && rightNephew == null)
						|| (leftNephew != null && rightNephew != null && !leftNephew.isRed && !rightNephew.isRed)) {
					brotherNode.isRed = true;
					if (parentNode.isRed)
						parentNode.isRed = false;
					else
						balanceDeletion(parentNode.parent, parentNode);
					return;
				}
				if (leftNephew != null && leftNephew.isRed) {
					rotateToRight(parentNode, this::changeColor3);
					return;
				}
				if (rightNephew != null && rightNephew.isRed) {
					rotateToLeft(brotherNode, this::changeColor2);
					rotateToRight(parentNode, this::changeColor3);
					return;
				}

			}
		}
	}

	/*
	 * 右旋
	 */
	private void rotateToRight(Node<T> grandpaNode, BiFunction<Node<T>, Node<T>, Consumer<Node<T>>> changeColor) {
		Node<T> parentNode = grandpaNode.left;
		if (parentNode == null)
			return;
		changeColor.apply(grandpaNode, parentNode).accept(parentNode.left);
		Node<T> rightChildNode = parentNode.right;
		Node<T> ggrandpaNode = grandpaNode.parent;
		if (grandpaNode == root) {
			root = parentNode;
		} else {
			boolean isLeft = grandpaNode.isLeftNode();
			if (isLeft)
				ggrandpaNode.left = parentNode;
			else
				ggrandpaNode.right = parentNode;
		}
		parentNode.parent = ggrandpaNode;
		parentNode.right = grandpaNode;
		grandpaNode.left = rightChildNode;
		grandpaNode.parent = parentNode;
		if (rightChildNode != null)
			rightChildNode.parent = grandpaNode;
		updateRightRotateHigh(parentNode, grandpaNode);
	}

	/*
	 * 更新右旋时高度
	 */
	private void updateRightRotateHigh(Node<T> parentNode, Node<T> grandpaNode) {
		// parent节点和其左子树高度都减1
		parentNode.high = parentNode.high - 1;
		Node<T> leftNode = parentNode.left;
		if (leftNode != null)
			decreaseHigh(Arrays.asList(leftNode));
		// grandpa节点和其右子树高度都加1
		grandpaNode.high = grandpaNode.high + 1;
		Node<T> rightNode = grandpaNode.right;
		if (rightNode != null)
			increaseHigh(Arrays.asList(rightNode));
	}

	/*
	 * 左旋
	 */
	private void rotateToLeft(Node<T> grandpaNode, BiFunction<Node<T>, Node<T>, Consumer<Node<T>>> changeColor) {
		Node<T> parentNode = grandpaNode.right;
		if (parentNode == null)
			return;
		changeColor.apply(grandpaNode, parentNode).accept(parentNode.right);
		Node<T> leftChildNode = parentNode.left;
		Node<T> ggrandpaNode = grandpaNode.parent;
		if (grandpaNode == root) {
			root = parentNode;
		} else {
			boolean isLeft = grandpaNode.isLeftNode();
			if (isLeft)
				ggrandpaNode.left = parentNode;
			else
				ggrandpaNode.right = parentNode;
		}
		parentNode.parent = ggrandpaNode;
		parentNode.left = grandpaNode;
		grandpaNode.right = leftChildNode;
		grandpaNode.parent = parentNode;
		if (leftChildNode != null)
			leftChildNode.parent = grandpaNode;
		updateLeftRotateHigh(parentNode, grandpaNode);
	}

	// 节点不变色
	private Consumer<Node<T>> changeColor0(Node<T> grandpaNode, Node<T> parentNode) {
		return e -> {
		};
	}

	// 两个节点变色
	private Consumer<Node<T>> changeColor2(Node<T> grandpaNode, Node<T> parentNode) {
		grandpaNode.isRed = true;
		parentNode.isRed = false;
		return e -> {
		};
	}

	// 三个节点变色
	private Consumer<Node<T>> changeColor3(Node<T> grandpaNode, Node<T> parentNode) {
		parentNode.isRed = grandpaNode.isRed;
		grandpaNode.isRed = false;
		return e -> {
			e.isRed = false;
		};
	}

	/*
	 * 更新左旋时高度
	 */
	private void updateLeftRotateHigh(Node<T> parentNode, Node<T> grandpaNode) {
		// parent节点和其左子树高度都减1
		parentNode.high = parentNode.high - 1;
		Node<T> rightNode = parentNode.right;
		if (rightNode != null)
			decreaseHigh(Arrays.asList(rightNode));
		// grandpa节点和其右子树高度都加1
		grandpaNode.high = grandpaNode.high + 1;
		Node<T> leftNode = grandpaNode.left;
		if (leftNode != null)
			increaseHigh(Arrays.asList(leftNode));
	}

	private void decreaseHigh(List<Node<T>> nodeList) {
		List<Node<T>> childList = new ArrayList<>();
		for (Node<T> node : nodeList) {
			node.high = node.high - 1;
			if (node.left != null)
				childList.add(node.left);
			if (node.right != null)
				childList.add(node.right);
		}
		if (!childList.isEmpty())
			decreaseHigh(childList);
	}

	private void increaseHigh(List<Node<T>> nodeList) {
		List<Node<T>> childList = new ArrayList<>();
		for (Node<T> node : nodeList) {
			node.high = node.high + 1;
			if (node.left != null)
				childList.add(node.left);
			if (node.right != null)
				childList.add(node.right);
		}
		if (!childList.isEmpty())
			increaseHigh(childList);
	}

	public int getTreeHigh() {
		return totalHigh;
	}

	public void printTree() {
		if (root == null) {
			System.out.println("rb tree is empty");
			return;
		}
//		int x = 8,y=2;
//		int maxWidth = (int) (Math.pow(2, totalHigh-2)*(x+y)-y);
//		int gap = maxWidth/2;
		List<Node<T>> nodeList = Arrays.asList(root);
		printTree(nodeList);
	}

	private void printTree(List<Node<T>> nodeList) {
		List<Node<T>> childList = new ArrayList<>();
		for (Node<T> node : nodeList) {
			node.printNode();
			if (node.left != null)
				childList.add(node.left);
			if (node.right != null)
				childList.add(node.right);
		}
		if (!childList.isEmpty())
			printTree(childList);
	}

	// 插入逻辑
	/*
	 * P is parent node,U is uncle node,G is grandpa node L is left node,R is right
	 * node R is red color,B is Black color rotateToRight(Node n),rotateToLeft(Node
	 * n)
	 */

	// 删除逻辑
	/*
	 * 1、删除节点没有子节点，直接删除 2、删除节点只有一个子节点。那么删除节点一定是黑节点，用子节点代替删除节点
	 * 3、删除节点有两个子节点，找到删除节点的后继节点，将后继节点的值赋值给删除节点，删除后继节点
	 */
}
