package com.machao.learning.algorithm.HeapSort;

import java.util.Arrays;

public class HeapSortTest {

	// 需要排序的数组
	static int a[] = { 49, 38, 65, 97, 76, 13, 27, 49 };

	// main
	public static void main(String[] args) {
		heapSort(a);
	}

	/**
	 * 堆排序函数
	 * 
	 * @param a
	 *            需要排序的数组
	 */
	public static void heapSort(int[] a) {
		// 数据检查
		if (a == null || a.length == 0)
			return;
		int l = a.length - 1;// 记录数组最后一个元素下标
		for (int i = 0; i < l; i++) {// 循环调用建立堆函数建立堆
			buildHeap(a, l - i);// 调用建立堆函数
			swap(a, 0, l - i);// 堆建立好后，交换根节点和堆的最后一个节点
			System.out.println(Arrays.toString(a));
		}
	}

	/**
	 * 交换数组中指定的节点元素
	 * 
	 * @param a
	 * @param i
	 * @param j
	 */
	public static void swap(int[] a, int i, int j) {
		int tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}

	/**
	 * 建立堆函数，在数组a中，从0到lastIndex建立堆
	 * 
	 * @param a
	 * @param lastIndex
	 */
	public static void buildHeap(int[] a, int lastIndex) {
		for (int i = (lastIndex - 1) / 2; i >= 0; i--) {// 从lastIndex节点（最后一个节点）的父节点开始循环
			int k = i;// k保存正在判断的节点
			int biggerIndex;// k节点的左子节点索引
			while ((biggerIndex = k * 2 + 1) <= lastIndex) {// 如果当前k节点的子节点存在
				if (biggerIndex < lastIndex) {// 表示k节点存在右节点
					if (a[biggerIndex] < a[biggerIndex + 1]) {
						biggerIndex++;// biggerIndex总是记录较大子节点的索引
					}
				}
				if (a[k] < a[biggerIndex]) {// 如果k节点的值小于较大子节点的值，则交换它们的值，并将biggerIndex赋值给k，开始下一次循环
					swap(a, k, biggerIndex);// 保证k节点的值大于其子节点的值
					k = biggerIndex;// 赋值
				} else {
					break;
				}

			}
		}
	}

}
