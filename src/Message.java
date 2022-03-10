import java.util.StringTokenizer;

public class Message {
    private String timestamp;
    private String sourceid;
    private String messagecontent;

    public Message (String a,String fromid,String b) {
        this.timestamp=a;
        this.messagecontent=b;
        this.sourceid=fromid;
    }
    public  static String encoder(Message message){
        String str=message.getTimestamp()+"+"+message.getSourceid()+"+"+message.getMessagecontent();
        return str;
    }
    public static void decoder(String msg,Message message){
        StringTokenizer stringTokenizer=new StringTokenizer(msg,"+");
        message.setTimestamp(stringTokenizer.nextToken());
        message.setSourceid(stringTokenizer.nextToken());
        message.setMessagecontent(stringTokenizer.nextToken());
    }


    public String getTimestamp() {
        return timestamp;
    }

    public String getSourceid() {
        return sourceid;
    }

    public String getMessagecontent() {
        return messagecontent;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
    }

    public void setMessagecontent(String messagecontent) {
        this.messagecontent = messagecontent;
    }

}
