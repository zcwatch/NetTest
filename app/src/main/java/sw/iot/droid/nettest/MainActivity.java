package sw.iot.droid.nettest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "NetworkTest";
    private EditText socketIP;
    private EditText socketPort;
    private TextView textResult;
    private TextView textDebug;
    private Button buttonSend;
    private String[] serverNames;
    private String[] serverValues;
    private TextView textIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socketIP = (EditText) findViewById(R.id.editServer);
        socketPort = (EditText) findViewById(R.id.editPort);
        textResult = (TextView) findViewById(R.id.textResult);
        textDebug = (TextView) findViewById(R.id.textDebug);
        textIP = (TextView) findViewById(R.id.textIP);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        ((Button)findViewById(R.id.buttonLocal)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIP.setText("127.0.0.1");
                socketPort.setText("55555");
            }
        });
        ((Button)findViewById(R.id.buttonReset)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIP.setText("10.201.78.74");
                socketPort.setText("6002");
            }
        });

        serverNames = getResources().getStringArray(R.array.server_names);
        serverValues = getResources().getStringArray(R.array.server_values);
        NameAdapter adapter = new NameAdapter(this, serverNames, serverValues);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "spinner onItemSelected " + i);
                String server = serverValues[i];
                int k = server.indexOf(':');
                socketIP.setText(server.substring(0, k));
                socketPort.setText(server.substring(k+1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final List<NameAddress> listPeople = new ArrayList<NameAddress>();
        listPeople.add(new NameAddress("张三", "上海 "));
        listPeople.add(new NameAddress("李四", "上海 "));
        listPeople.add(new NameAddress("王五", "北京" ));
        listPeople.add(new NameAddress("赵六", "广州 "));
        NameAdapter adapter2 = new NameAdapter(this, listPeople);

        //ArrayAdapter<String> adapter3=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, serverValues);
        //adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner2.setAdapter(adapter2);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NameAddress people = listPeople.get(i);
                textDebug.setText(people.getName()+" at "+people.getAddress());
                //textDebug.setText(serverValues[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        BroadcastReceiver connReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                    if (activeNetwork != null) {   // connected to the internet
                        if (activeNetwork.isConnected()) {
                            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                                // connected to wifi
                            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                                // connected to the mobile provider's data plan
                            }
                        } else {
                            Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                        }
                        textIP.setText(getHostIP());

                        Log.e(TAG, "info.getTypeName()" + activeNetwork.getTypeName());
                        Log.e(TAG, "getSubtypeName()" + activeNetwork.getSubtypeName());
                        Log.e(TAG, "getState()" + activeNetwork.getState());
                        Log.e(TAG, "getDetailedState()" + activeNetwork.getDetailedState().name());
                        Log.e(TAG, "getDetailedState()" + activeNetwork.getExtraInfo());
                        Log.e(TAG, "getType()" + activeNetwork.getType());
                    } else {   // not connected to the internet
                        Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                    }
                }
            }
        };
        registerReceiver(connReceiver, filter);
    }

    public void onSend(View v) {
        textResult.setText("Connecting...");
        buttonSend.setEnabled(false);
        String ip = socketIP.getText().toString();
        int port = Integer.parseInt(socketPort.getText().toString());
        doTest(ip, port);
    }

    private final int TEST_RESPONSE = 1000;
    String result;
    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TEST_RESPONSE:
                    textResult.setText(result);
                    buttonSend.setEnabled(true);
                    break;
            }
            //super.handleMessage(msg);
        }
    };

    public void doTest(final String ip, final int port) {
        final String server = ip+":"+port;
        //textDebug.setText("Server="+server);
        new Thread() {
            @Override
            public void run() {
                Socket socket = null;

                try {
                    socket = new Socket(ip, port);
                    socket.setSoTimeout(30*1000);
                    Log.e(TAG, "建立连接：" + socket);
                    result = server+"连接成功！";
                } catch (UnknownHostException e) {
                    result = e.getMessage();
                    e.printStackTrace();
                } catch (IOException e) {
                    result = e.getMessage();
                    e.printStackTrace();
                }
                if (socket!=null && !socket.isConnected()) result = server+"连接失败！\r\n";

                try {
                    if (socket!=null) socket.close();
                } catch (IOException e) {
                    result = e.getMessage();
                }

                myHandler.sendEmptyMessage(TEST_RESPONSE);
            }
        }.start();
    }

    public static String getHostIP() {
        String hostIp = "127.0.0.1";
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) continue;
                    String ip = ia.getHostAddress();
                    if (!hostIp.equals(ip)) {
                        hostIp = ip;
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }
}
