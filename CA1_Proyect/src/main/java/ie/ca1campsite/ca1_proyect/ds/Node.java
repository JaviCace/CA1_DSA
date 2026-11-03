package ie.ca1campsite.ca1_proyect.ds;


// A node is one "box" of the linked list.
// It stores one piece of data and a link to the next node.
public class Node<T> {
    private T data;
    private Node<T> next;

    // Create a node with some data (no next node yet)
    public Node(T data) {
        this.data = data;
        this.next = null;
    }

    public T getData() {
        return data;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }
}