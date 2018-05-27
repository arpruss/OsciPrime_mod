
    /**
    OsciPrime an Open Source Android Oscilloscope
    Copyright (C) 2012  Manuel Di Cerbo, Nexus-Computing GmbH Switzerland
    Copyright (C) 2012  Andreas Rudolf, Nexus-Computing GmbH Switzerland

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */

package ch.nexuscomputing.android.osciprimeics.source;


import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import ch.nexuscomputing.android.osciprimeics.IServiceInterface;
import ch.nexuscomputing.android.osciprimeics.OsciPrimeApplication;
import ch.nexuscomputing.android.usb.IUsbConnectionHandler;
import ch.nexuscomputing.android.usb.UsbController;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

public class OsciUsbSerialSource implements Source {
    private static final int BAUD_RATE = 115200;
    private final IServiceInterface mSvc;
    private final OsciPrimeApplication mApplication;
    private final UsbManager mUsbManager;
    private final UsbDeviceConnection mConnection;
    private Thread mMainThread = null;
    private UsbDevice mDevice;
    private UsbController mUsbCont;
    private UsbSerialDevice serialPort;
    private boolean serialPortConnected = false;

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data) {
        }
    };

    public OsciUsbSerialSource(IServiceInterface svc, OsciPrimeApplication app) {
        mSvc = svc;
        mApplication = app;
        mUsbManager = (UsbManager) app.getSystemService(Context.USB_SERVICE);
        mUsbCont = new UsbController(app, mSvc, new IUsbConnectionHandler() {
            @Override
            public void onDeviceNotFound() {
                mSvc.onError();
                return;
            }

            @Override
            public void onDeviceInitialized(UsbDevice dev) {
                if (mSvc.stopNow()) {
                    return;
                }
                // open USB
            }
        }, true);
        mConnection = mUsbManager.openDevice(mDevice);
        // TODO: move to thread
    }

    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            serialPort = UsbSerialDevice.createUsbSerialDevice(mDevice, mConnection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    serialPortConnected = true;
                    serialPort.setBaudRate(BAUD_RATE);
                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    /**
                     * Current flow control Options:
                     * UsbSerialInterface.FLOW_CONTROL_OFF
                     * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
                     * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
                     */
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(mCallback);
                }
            }
        }
    }

    @Override
    public float getVoltageDivsion(int channel, int setting) {
        return 0;
    }

    @Override
    public float getDefaultAttenuation(int channel, int setting) {
        return 0;
    }

    @Override
    public void attenuationChanged(OsciPrimeApplication app) {

    }

    @Override
    public void resample(boolean retrigger) {

    }

    @Override
    public int getResolutionInBits() {
        return 8; //TODO: variable
    }

    @Override
    public void forceStop() {

    }

    @Override
    public void resample(boolean retrigger, int index) {

    }
}
