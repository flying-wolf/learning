package com.machao.learning.concurrent.LinkedBlockingQueue.test;

public class LastNode {

	static class Node<E> {
		E item;

		/**
		 * One of: - the real successor Node - this Node, meaning the successor is
		 * head.next - null, meaning there is no successor (this is the last node)
		 */
		Node<E> next;

		Node(E x) {
			item = x;
		}
	}

	public static void main(String[] args) {
		Node<String> last;

		last = new Node<String>(null);
		Node<String> node = new Node<String>("123");
		// last = last.next = node;
		last.next = node;
		System.err.println(last.next.item);
		last = last.next;

		System.err.println(last.item);
		System.err.println(last.next.item);

	}
}
