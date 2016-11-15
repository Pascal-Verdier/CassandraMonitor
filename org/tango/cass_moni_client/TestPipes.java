//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// :  $
//
//-======================================================================

package org.tango.cass_moni_client ;

import fr.esrf.Tango.DevEncoded;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This class is able to display pipe content
 *
 * @author verdier
 */

public class TestPipes {
    private DeviceProxy deviceProxy;

    //===============================================================
    //===============================================================
    public static void display(DevicePipe devicePipe) throws DevFailed {
        System.out.println(devicePipe.getPipeName() + ": " +
                new Date(devicePipe.getTimeValMillisSec()));
        display(devicePipe.getPipeBlob());
    }
    //===============================================================
    //===============================================================
    private static int deep = 0;
    public static void display(PipeBlob pipeBlob) throws DevFailed {
        deep++;
        System.out.println(indent() + "["+ pipeBlob.getName() + "]:");
        for (PipeDataElement dataElement : pipeBlob) {
            int type = dataElement.getType();
            System.out.println(indent() + " - " + dataElement.getName() +
                    ":  " + TangoConst.Tango_CmdArgTypeName[type]);
            switch(type) {
                case TangoConst.Tango_DEV_PIPE_BLOB:
                    //  Do a re entrance to display inner blob
                    display(dataElement.extractPipeBlob());
                    break;

                case TangoConst.Tango_DEV_BOOLEAN:
                    display(dataElement.extractBooleanArray());
                    break;
                case TangoConst.Tango_DEV_CHAR:
                    display(dataElement.extractCharArray());
                    break;
                case TangoConst.Tango_DEV_UCHAR:
                    display(dataElement.extractUCharArray());
                    break;
                case TangoConst.Tango_DEV_SHORT:
                    display(dataElement.extractShortArray());
                    break;
                case TangoConst.Tango_DEV_USHORT:
                    display(dataElement.extractUShortArray());
                    break;
                case TangoConst.Tango_DEV_LONG:
                    display(dataElement.extractLongArray());
                    break;
                case TangoConst.Tango_DEV_ULONG:
                    display(dataElement.extractULongArray());
                    break;
                case TangoConst.Tango_DEV_LONG64:
                    display(dataElement.extractLong64Array());
                    break;
                case TangoConst.Tango_DEV_DOUBLE:
                    display(dataElement.extractDoubleArray());
                    break;
                case TangoConst.Tango_DEV_FLOAT:
                    display(dataElement.extractFloatArray());
                    break;
                case TangoConst.Tango_DEV_STRING:
                    display(dataElement.extractStringArray());
                    break;
                case TangoConst.Tango_DEV_STATE:
                    display(dataElement.extractDevStateArray());
                    break;
                case TangoConst.Tango_DEV_ENCODED:
                    display(dataElement.extractDevEncodedArray());
                    break;
            }
        }
        deep--;
    }
    //===============================================================
    //===============================================================
    private static void display(boolean[] array) {
        for (boolean b : array)
            System.out.println(indent()+"    " + b);
    }
    //===============================================================
    //===============================================================
    private static void display(byte[] array) {
        for (byte b : array)
            System.out.println(indent()+"    " + b);
    }
    //===============================================================
    //===============================================================
    private static void display(String[] array) {
        for (String s : array)
            System.out.println(indent()+"    " + s);
    }
    //===============================================================
    //===============================================================
    private static void display(short[] array) {
        for (short s : array)
            System.out.println(indent()+"    " + s);
    }
    //===============================================================
    //===============================================================
    private static void display(int[] array) {
        for (int i : array)
            System.out.println(indent()+"    " + i);
    }
    //===============================================================
    //===============================================================
    private static void display(long[] array) {
        for (long l : array)
            System.out.println(indent()+"    " + l);
    }
    //===============================================================
    //===============================================================
    private static void display(double[] array) {
        for (double d : array)
            System.out.println(indent()+"    " + d);
    }
    //===============================================================
    //===============================================================
    private static void display(float[] array) {
        for (float f : array)
            System.out.println(indent()+"    " + f);
    }
    //===============================================================
    //===============================================================
    private static void display(DevState[] array) {
        for (DevState state : array)
            System.out.println(indent()+"    " + ApiUtil.stateName(state));
    }
    //===============================================================
    //===============================================================
    private static void display(DevEncoded[] array) {
        for (DevEncoded e : array)
            System.out.println(indent()+"    Encoded format: " + e.encoded_format);
    }
    //===============================================================
    //===============================================================
    private static String indent() {
        String s = "";
        for (int i=0 ; i<deep ; i++) {
            s += "    ";
        }
        return s;
    }
    //===============================================================
    //===============================================================
}
