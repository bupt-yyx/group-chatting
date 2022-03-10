import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class ChatServer {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private void createserver(){
        try {
            //创建serverChannel
            serverSocketChannel = ServerSocketChannel.open();
            //设置非阻塞
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(1111));
            //打开选择器
            selector = Selector.open();
            //注册监听器
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
            //监听器监听
            monitor(serverSocketChannel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void monitor(ServerSocketChannel serverSocketChannel) throws IOException, ClosedChannelException {
        System.out.println("服务器已经启动...");
        // 会阻塞 直到有事件产生
        while(selector.select()>0){
            //获取所有有事件的selectionKey
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()){
                SelectionKey key = it.next();
                //判断如果是accept事件 进行操作
                if(key.isAcceptable()){
                    //从selectionKey中获取channel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //将连接进来的客户端 设置为非阻塞 并注册到seletor上
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println(socketChannel.getRemoteAddress().toString()+"上线");
                }
                //监听读事件  也就是从客户端发来的数据
                if(key.isReadable()){
                    //打印数据
                    handle(key);
                }
                //将该socketChannel事件处理完以后要从selector中移除掉，防止死循环
                it.remove();
            }
        }
    }

    private String getRemoteAddress(SelectionKey key) {
        String address = null;
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            address = socketChannel.getRemoteAddress().toString();
        } catch (Exception e) {
        }
        return address;
    }


    private void handle(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        String address = getRemoteAddress(key);
        try {
            StringBuilder stringBuilder = new StringBuilder();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int len = 0;
            while((len=socketChannel.read(byteBuffer)) >0){
                byteBuffer.flip();
                String str=new String(byteBuffer.array(),0,len);
                if(!str.equals("exit"))
                {
                    stringBuilder.append(str);
                    byteBuffer.clear();
                }
                else
                {
                    byteBuffer.clear();
                    //如果接受消息失败表示离线
                    key.cancel();
                    System.out.println(address+"客户端退出");
                }
            }
            if(stringBuilder.toString().length()>0){
                String str=stringBuilder.toString();
                Message message=new Message(null,null,null);
                Message.decoder(str,message);
                System.out.println(message.getSourceid()+"端口发送"+message.getMessagecontent()+"消息");
                //转发数据
                sendHandle(key,stringBuilder.toString());
            }
        } catch (Exception e) {
            //如果接受消息失败表示离线
            key.cancel();
            System.out.println(address+"客户端关闭");
            try {
                socketChannel.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
    private void sendHandle(SelectionKey key,String msg){
        try {
            Set<SelectionKey> set = selector.keys();
            for(SelectionKey clientKey : set){
                Channel channel = clientKey.channel();
                if(channel instanceof SocketChannel && clientKey != key){
                    SocketChannel socketChannel = (SocketChannel)channel;
                    System.out.println("发送该消息给"+socketChannel.getRemoteAddress()+"客户端");
                    socketChannel.write(ByteBuffer.wrap(String.valueOf(msg).getBytes()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        ChatServer chatServer = new ChatServer();
        chatServer.createserver();
    }
}
