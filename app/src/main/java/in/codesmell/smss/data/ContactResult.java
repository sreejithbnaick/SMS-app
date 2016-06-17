package in.codesmell.smss.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sreejith on 17/6/16.
 */
public class ContactResult {
    public List<ContactData> contacts= new ArrayList<>();
    public HashMap<String,ArrayList<ContactData>> contactMap = new HashMap<String,ArrayList<ContactData>>();
}
