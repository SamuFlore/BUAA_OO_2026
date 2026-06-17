import com.oocourse.elevator3.PersonRequest;

public class MyPersonReq {
    private final PersonRequest personRequest;
    private final String fromFloor;

    public MyPersonReq(PersonRequest personRequest, String fromFloor) {
        this.personRequest = personRequest;
        this.fromFloor = fromFloor;
    }

    public String getFromFloor() {
        return this.fromFloor;
    }

    public String getToFloor() {
        return personRequest.getToFloor();
    }

    public int getPersonId() {
        return personRequest.getPersonId();
    }

    public int getWeight() { return personRequest.getWeight(); }

    public PersonRequest getPersonReq() { return personRequest; }
}
