package com.quki.example.testbluetoothconnectedwithpc;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private BluetoothAdapter mBluetoothAdapter;
	private final int REQUEST_ENABLE_BT = 1;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.listView);

		// get the bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// check if the device has bluetooth capabilities
		// if not, display a toast message and close the app
		if (mBluetoothAdapter == null) {

			Toast.makeText(this, "This app requires a bluetooth capable phone",
					Toast.LENGTH_SHORT).show();
			finish();
		}

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {

		// check if bluetooth is enabled
		// if not, ask the user to enable it using an Intent
		if (!mBluetoothAdapter.isEnabled()) {

			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		// if the bluetooth is enabled, display paired devices
		else
			listPairedDevices();

		super.onPostCreate(savedInstanceState);
	}

	private void listPairedDevices() {

		// get the paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		ArrayList<String> pairedDeviceList = new ArrayList<String>();
		for (BluetoothDevice pairedDevice : pairedDevices) {
			pairedDeviceList.add(pairedDevice.getName());
		}
		ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.list_items, pairedDeviceList);
		listView.setAdapter(mArrayAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String deviceName = (String) parent.getItemAtPosition(position);
				Toast.makeText(getApplicationContext(), deviceName,
						Toast.LENGTH_SHORT).show();
				transferToDevice(deviceName);
			}

		});

	}

	private void transferToDevice(String deviceName) {

		// UUID 설정 (SPP)
		UUID SPP_UUID = java.util.UUID
				.fromString("00001101-0000-1000-8000-00805F9B34FB");
		/*
		 * 클릭해서 가져온 Device의 String 값과 페어링 된 것이 일치하는지 한번 더 확인 후
		 * BluetoothDevice 객체로 target저장
		 */
		BluetoothDevice targetDevice = null;
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		for (BluetoothDevice pairedDevice : pairedDevices) {
			if (pairedDevice.getName().equals(deviceName)) {
				targetDevice = pairedDevice;
				break;
			}
		}
		
		// If the device was not found, toast an error and return
		if(targetDevice==null){
			Toast.makeText(getApplicationContext(), "Cannot found any devices", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Create a connection to the device with the SPP UUID
		BluetoothSocket mBluetoothSocket = null;
		try {
			mBluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(SPP_UUID);
		} catch (IOException e) {
			 Toast.makeText(this, "Unable to open a serial socket with the device", Toast.LENGTH_SHORT).show();
			 e.printStackTrace();
			 return;
		}
		
		// Connect to the device
		try {
			mBluetoothSocket.connect();
		} catch (IOException e) {
			 Toast.makeText(this, "Unable to connect with the device", Toast.LENGTH_SHORT).show();
			 e.printStackTrace();
			 return;
		}
		
		// Write the data by using OutputStreamWriter
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mBluetoothSocket.getOutputStream());
			outputStreamWriter.write("Ahn Seung Hwan Byungsin!/r/n");
			outputStreamWriter.flush();
		} catch (IOException e) {
			Toast.makeText(this, "Unable to send message to the device", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		
		try {
			mBluetoothSocket.close();
			Toast.makeText(this, "Success to send message to the device", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, "Fail to close the connection to device", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		
	}

}
