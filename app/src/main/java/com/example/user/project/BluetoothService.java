package com.example.user.project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothService { // Debugging
    private static final String TAG = "BluetoothService";
    private BluetoothAdapter btAdapter;
    private AppCompatActivity mActivity;

    private Handler mHandler; // Constructors

    private static  String[] result;
    public static boolean isConnect = false;
    public static boolean isRecive = false;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int STATE_NONE = 0; // we're doing nothing
    private static final int STATE_LISTEN = 1; // now listening for incoming connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote device
    private static int mState;

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static class LazyHolder{
        public static BluetoothService INSTANCE = new BluetoothService(MainActivity.getsApplication(), MainActivity.getmHandler());
    }

    public static BluetoothService getInstance(){
        return LazyHolder.INSTANCE;
    }

    public static String[] getResult(){ return result; }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        //protected Handler handler = new Handler();

        public ConnectedThread(BluetoothSocket socket)
        {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket; InputStream tmpIn = null;
            OutputStream tmpOut = null; // BluetoothSocket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            Log.d(TAG, "mmInStream is...." + mmInStream +"");
        }

        public synchronized void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            // Keep listening to the InputStream while connected
            int readBufferPosition = 0;
            byte[] readBuffer = new byte[1024];

            while (mState == STATE_CONNECTED)
            {
                try { // InputStream으로부터 값을 받는 읽는 부분(값을 받는다)

                    int byteAvailable = mmInStream.available();   // 수신 데이터 확인 읽을 수 있는 바이트 수
                    if(byteAvailable > 0) {                        // 데이터가 수신된 경우. 수신된 바이트가 0 이상일 경우

                        byte[] packetBytes = new byte[byteAvailable];   //수신된 데이터 수 만큼 바이트배열 생성

                        // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                        mmInStream.read(packetBytes);       //수신된 데이터만큼의 바이트만 읽음

                        for (int i = 0; i < byteAvailable; i++) {   //수신된 데이터 수만큼 반복
                            byte b = packetBytes[i];        //읽어들인 한 바이트
                            if (b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;

                                Log.d(TAG, data);

                                if(data.split("/").length == 8) {
                                    result = data.split("/");
                                    isRecive = true;
                                }else{
                                    isRecive = false;
                                }

                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }

                    Thread.sleep(500);

                } catch (Exception e) {
                    Log.i(TAG, "in catch");
                    Log.e(TAG, "disconnected", e);
                }
            }
        }

        /** * Write to the connected OutStream. * @param buffer The bytes to write */
        public void write(byte[] buffer)
        {
            try {
                // 값을 쓰는 부분(값을 보낸다)
                mmOutStream.write(buffer);
                //mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();

                Log.i(TAG, "write success");
            } catch (IOException e)
            {
                Log.e(TAG, "Exception during write", e);
            }

        }

        public void cancel()
        {
            try {
                mmSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // 디바이스 정보를 얻어서 BluetoothSocket 생성
            try {
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp = createBluetoothSocket(mmDevice);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        //-----------
        private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
                throws IOException {
            if(Build.VERSION.SDK_INT >= 10){
                try {
                    final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                    return (BluetoothSocket) m.invoke(device, MY_UUID);
                } catch (Exception e) {
                    Log.e(TAG, "Could not create Insecure RFComm Connection",e);
                }
            }
            return  device.createRfcommSocketToServiceRecord(MY_UUID);
        }
        //-----------

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
            // 기기 검색이 계속되면 연결속도가 느려지기 때문이다.
            btAdapter.cancelDiscovery();

            // BluetoothSocket 연결 시도
            try {
                // BluetoothSocket 연결 시도에 대한 return 값은 succes 또는 exception이다.
                mmSocket.connect();
                Log.d(TAG, "Connect Success");
                isConnect = true;

            } catch (IOException e) {
                connectionFailed();		// 연결 실패시 불러오는 메소드
                Log.d(TAG, "Connect Fail");

                Log.e(TAG, e.getMessage());
                // socket을 닫는다.
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                // 연결중? 혹은 연결 대기상태인 메소드를 호출한다.
                BluetoothService.this.start();
                return;
            }

            // ConnectThread 클래스를 reset한다.
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // ConnectThread를 시작한다.
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public void scanDevice() {
        Log.d(TAG, "Scan Device");
        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");

        if(btAdapter == null)
        {
            Log.d(TAG, "Bluetooth is not available");
            return false;
        }
        else
        {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }

    public void getDeviceInfo(Intent data)
    {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        // Get the BluetoothDevice object
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        connect(device);
    }

    public void enableBluetooth()
    {
        Log.i(TAG, "Check the enabled Bluetooth");
        if(btAdapter.isEnabled()) {
            // 기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable Now");
            scanDevice();
            // Next Step
        }
        else
        {	// 기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "Bluetooth Enable Request");
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    private BluetoothService(AppCompatActivity ac, Handler h) {
        mActivity = ac;
        mHandler = h; // BluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // Bluetooth 상태 set
    private synchronized void setState(int state)
    {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    // Bluetooth 상태 get
    public synchronized int getState()
    {
        return mState;
    }

    public synchronized void start()
    {
        Log.d(TAG, "start"); // Cancel any thread attempting to make a connection
        if (mConnectThread == null)
        { }
        else
        {
            mConnectThread.cancel();
        }
        mConnectThread = null;
        // Cancel any thread currently running a connection
        if (mConnectedThread == null)
        {

        }
        else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    // ConnectThread 초기화 device의 모든 연결 제거
    public synchronized void connect(BluetoothDevice device)
    {
        Log.d(TAG, "connect to: " + device); // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null)
            {

            }
            else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null)
        {

        }
        else
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        } // Start the thread to connect with the given device

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // ConnectedThread 초기화
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
    {
        Log.d(TAG, "connected"); // Cancel the thread that completed the connection
        if (mConnectThread == null)
        {

        }
        else {
            mConnectThread.cancel();
        }

        mConnectThread = null;
        // Cancel any thread currently running a connection
        if (mConnectedThread == null)
        {

        }
        else
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        } // Start the thread to manage the connection and perform transmissions

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        setState(STATE_CONNECTED);
    }

    // 모든 thread stop
    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);

    }

    // 값을 쓰는 부분(보내는 부분)
    public void write(byte[] out)
    { // Create temporary object
        ConnectedThread r; // Synchronize a copy of the ConnectedThread synchronized (this)
        {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        } // Perform the write unsynchronized

        r.write(out);
    }

    // 연결 실패했을때
    private void connectionFailed()
    {
        setState(STATE_LISTEN);
    } // 연결을 잃었을 때

    private void connectionLost()
    {
        setState(STATE_LISTEN);
    }

}

