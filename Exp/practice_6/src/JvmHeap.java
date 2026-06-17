import java.util.List;

public class JvmHeap extends MyHeap<MyObject> {//包含题目[1]
    JvmHeap(int capacity) {
        super(capacity);
    }
    
    /*@ public normal_behavior
      @ requires objectId != null;
      @ assignable elements[*].referenced;
      @ ensures size == \old(size);
      @ ensures (\forall int i; 1 <= i && i <= size &&
      @          (\forall int j; 0 <= j && j < objectId.size(); elements[i].getId() != objectId.get(j));
      @           elements[i].equals(\old(elements[i])));
      @ ensures (\forall int i; 1 <= i && i <= size;
      @          (\exists int j; 0 <= j && j < objectId.size();
      @            objectId.get(j) == elements[i].getId()) ==>  (!elements[i].isReferenced()));
      @*/
    public void setUnreferencedId(List<Integer> objectId) {
        for (int id : objectId) {
            for (int i = 1; i <= this.getSize(); i++) {
                MyObject myObject = this.getElement(i);
                if (myObject.getId() == id) {
                    myObject.setUnreferenced();
                    setelements(i, myObject);
                }
            }
        }
    }
    
    /*@ public normal_behavior
      @ assignable elements, size;
      @ ensures size == (\sum int i; 1 <= i && i <= \old(size) &&  \old(elements[i].isReferenced()); 1);
      @ ensures (\forall int i; 1 <= i && i <= \old(size);
      @          \old(elements[i].isReferenced()) ==>
      @           (\exists int j; 1 <= j && j <= size; elements[j].equals(\old(getElement(i)))));
      @ ensures (\forall int i; 1 <= i && i <= \old(size);
      @          !(\old(elements[i].isReferenced())) ==>
      @           (\forall int j; 1 <= j && j <= size;
      @           !elements[j].equals(\old(elements[i]))));
      @ ensures (\forall int i; 1 <= i && i <= size;
      @          (\exists int j; 1 <= j && j <= \old(size);
      @          elements[i].equals(\old(elements[j]))));
      @ ensures (\forall int i; 1 <= i && i < size; elements[i+1].compareTo(elements[i]) > 0);
      @*/
    public void removeUnreferenced() {
        Object[] elements = getElements();
       //[1]
        int oldSize = getSize();
        int newSize = 0;
        for (int i = 1; i <= oldSize; i++) {
            MyObject myObject = (MyObject) elements[i];
            if (myObject.isReferenced()) {
                newSize++;
                setelements(newSize, myObject);
            }
        }
        setSize(newSize);
        for (int i = 1; i <= newSize; i++) {
            for (int j = 1; j <= newSize - i - 1; j++) {
                MyObject obj1 = (MyObject) elements[j];
                MyObject obj2 = (MyObject) elements[j + 1];
                if (obj1.compareTo(obj2) > 0) {
                    Object tmp = new Object();
                    tmp = elements[j];
                    elements[j] = elements[j + 1];
                    elements[j + 1] = tmp;
                }
            }
        }
    }
    
    /*@ public normal_behavior
      @ requires size > 0;
      @ ensures (\forall int i; 1 <= i && i <= size; \result.compareTo(elements[i]) <= 0);
      @ ensures (\exists int i; 1 <= i && i <= size; \result == elements[i]);
      @ also
      @ public normal_behavior
      @ requires size == 0;
      @ ensures \result == null;
      @*/
    public /*@ pure @*/ MyObject getYoungestOne() {
        if (getSize() == 0) {
            return null;
        }
        return getElement(1);
    }
}