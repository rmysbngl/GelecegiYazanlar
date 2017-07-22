package com.j0n17.led;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Main extends Activity implements OnCheckedChangeListener {

	ToggleButton OnOff, tgConnect;
	TextView Result;
	private String dataToSend;

	private static final String TAG = "Jon";
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private static String address = "00:15:FF:F3:20:26";
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private InputStream inStream = null;
	Handler handler = new Handler();
	byte delimiter = 10;
	boolean stopWorker = false;
	int readBufferPosition = 0;
	byte[] readBuffer = new byte[1024];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tgConnect = (ToggleButton) findViewById(R.id.tgConnect);
		OnOff = (ToggleButton) findViewById(R.id.tgOnOff);
		Result = (TextView) findViewById(R.id.msgJonduino);

		// tgConnect.setOnClickListener(this);
		tgConnect.setOnCheckedChangeListener(this);
		OnOff.setOnCheckedChangeListener(this);
		CheckBt();
	}

	private void CheckBt() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(getApplicationContext(), "Bluetooth désactivé !",
					Toast.LENGTH_SHORT).show();
		}

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth inexistant ou occupé !", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect:
				Connect();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void Connect() {
		Log.d(TAG, address);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		Log.d(TAG, "Connexion en cours à " + device);
		mBluetoothAdapter.cancelDiscovery();
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			btSocket.connect();
			Log.d(TAG, "Connexion réussie !!");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.d(TAG, "Impossible de fermer la connexion.");
			}
			Log.d(TAG, "Création de socket échouée.");
		}
	}

	private void writeData(String data) {
		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.d(TAG, "Bug AVANT l'envoie.", e);
		}

		String message = data;

		byte[] msgBuffer = message.getBytes();

		try {
			outStream.write(msgBuffer);
		} catch (IOException e) {
			Log.d(TAG, "Bug DURANT l'envoie.", e);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			btSocket.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		switch (arg0.getId()) {
		case R.id.tgConnect:
			if (arg1) {
				Connect();
			} else {
				if (btSocket.isConnected()) {
					try {
						btSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			break;
		case R.id.tgOnOff:
			if (arg1) {
				dataToSend = "a";
				writeData(dataToSend);
			} else {
				dataToSend = "b";
				writeData(dataToSend);
			}
			break;
		}

	}

}
