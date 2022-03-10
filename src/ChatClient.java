import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    private Selector selector;
    private final String HOST = "127.0.0.1";
    private final int PORT = 1111;
    String localport = null;
    ExecutorService pool = Executors.newSingleThreadExecutor();
    public String timer() {
        Calendar c = Calendar.getInstance();
        String time;
        int month=c.get(Calendar.MONTH)+1;
        int date=c.get(Calendar.DATE);
        int hour=c.get(Calendar.HOUR);
        int minute=c.get(Calendar.MINUTE);
        int second=c.get(Calendar.SECOND);
        time=month+"月"+date+"日"+hour+":"+minute+":"+second;
        return time;
    }

    private void createclient(){
        try {
            //创建channel
            final SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            socketChannel.configureBlocking(false);
            selector = Selector.open();
            //注册监听器 监听读消息
            socketChannel.register(selector, SelectionKey.OP_READ);

            //异步输入
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    Scanner scanner = new Scanner(System.in);
                    while (scanner.hasNextLine()) {
                        String str = scanner.nextLine();
                        if(!str.equals("exit")) {
                            Message message = new Message(timer(), localport, str);
                        str=Message.encoder(message);
                        try {
                            socketChannel.write(ByteBuffer.wrap(str.getBytes()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                        else{
                            try {
                                socketChannel.write(ByteBuffer.wrap(str.getBytes()));
                                socketChannel.close();
                                pool.shutdownNow();
                                System.exit(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            String str = socketChannel.getLocalAddress().toString();
            String str1 =null;
            if (!str.isEmpty()) {
                StringTokenizer stringTokenizer = new StringTokenizer(str, ":");
                str1= stringTokenizer.nextToken();
                localport= stringTokenizer.nextToken();
            }
            System.out.println("我是：" +localport+"端口");
            while (selector.select() > 0) {
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        StringBuilder stringBuilder = new StringBuilder();
                        while (channel.read(byteBuffer) > 0) {
                            byteBuffer.flip();
                            String receive=new String(byteBuffer.array(), 0, byteBuffer.limit());
                            Message message=new Message(null,null,null);
                            Message.decoder(receive,message);
                            stringBuilder.append("从"+message.getSourceid()+"端口传入消息"+"\t");
                            stringBuilder.append("消息内容:"+message.getMessagecontent()+"\t");
                            stringBuilder.append("时间:"+message.getTimestamp());
                            byteBuffer.clear();
                        }
                        System.out.println(stringBuilder.toString());
                    }
                }
                it.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.createclient();
    }
}
