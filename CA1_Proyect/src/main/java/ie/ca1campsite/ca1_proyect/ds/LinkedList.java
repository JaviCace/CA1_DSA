package ie.ca1campsite.ca1_proyect.ds;

// A simple linked list built from scratch (no Java collections).
public class LinkedList<T> {
    private Node<T> head;  // first node of the list
    private int size;      // how many elements the list has

    // Create an empty list
    public LinkedList() {
        head = null;
        size = 0;
    }

    // Add an element at the end of the list
    public void add(T item) {
        Node<T> newNode = new Node<>(item);

        if (head == null) {
            // The list is empty, so the new node is the first one
            head = newNode;
        } else {
            // Walk to the last node and attach the new node after it
            Node<T> current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(newNode);
        }
        size++;
    }

    // Get the element at a position (the first position is 0)
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    // Remove the first element equal to item.
    // Returns true if it was found and removed, false otherwise.
    public boolean remove(T item) {
        if (head == null) {
            return false;
        }

        // Case 1: the element is in the first node
        if (head.getData().equals(item)) {
            head = head.getNext();
            size--;
            return true;
        }

        // Case 2: look for it in the rest of the list
        Node<T> current = head;
        while (current.getNext() != null) {
            if (current.getNext().getData().equals(item)) {
                // Skip the node we want to remove
                current.setNext(current.getNext().getNext());
                size--;
                return true;
            }
            current = current.getNext();
        }

        return false;
    }

    // Number of elements in the list
    public int size() {
        return size;
    }

    // True if the list has no elements
    public boolean isEmpty() {
        return size == 0;
    }

    // Remove all elements
    public void clear() {
        head = null;
        size = 0;
    }

    // Used to walk through the list from outside this class
    public Node<T> getHead() {
        return head;
    }
}