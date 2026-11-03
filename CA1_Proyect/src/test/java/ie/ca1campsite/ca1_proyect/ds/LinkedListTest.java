package ie.ca1campsite.ca1_proyect.ds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTest {

    @Test
    void addIncreasesSize() {
        LinkedList<String> list = new LinkedList<>();
        list.add("A");
        list.add("B");
        assertEquals(2, list.size());
    }

    @Test
    void removeMiddleElement() {
        LinkedList<String> list = new LinkedList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        assertTrue(list.remove("B"));
        assertEquals(2, list.size());
        assertEquals("C", list.get(1));
    }
}