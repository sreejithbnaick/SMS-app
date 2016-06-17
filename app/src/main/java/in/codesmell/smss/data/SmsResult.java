package in.codesmell.smss.data;

import java.util.ArrayList;
import java.util.HashMap;

public class SmsResult {
    public ArrayList<SmsData> groupData = new ArrayList<>();
    public HashMap<String, ArrayList<SmsData>> groupDataMap = new HashMap<>();
}