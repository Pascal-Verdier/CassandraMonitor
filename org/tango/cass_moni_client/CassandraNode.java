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

package org.tango.cass_moni_client;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DevicePipe;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.PipeBlob;
import fr.esrf.TangoApi.PipeDataElement;
import fr.esrf.TangoApi.events.ITangoPipeListener;
import fr.esrf.TangoApi.events.TangoEventsAdapter;
import fr.esrf.TangoApi.events.TangoPipeEvent;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.core.AttributeList;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.IDevStateScalar;
import fr.esrf.tangoatk.widget.attribute.StateViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is able to
 *
 * @author verdier
 */

public class CassandraNode extends DeviceProxy {

    private String name;
    private List<Compaction> compactionList = new ArrayList<>();
    private StateViewer stateViewer;
    private AttributeList attributeList = new AttributeList();
    private JRadioButton compactionButton;
    private CompactionChart compactionChart;

    private static final String pipeName = "Compactions";
    private static final Font   font = new Font("Dialog", Font.BOLD, 12);
    public static final int COMPACTION = 0;
    public static final int VALIDATION = 1;
    //===============================================================
    //===============================================================
    public CassandraNode(String deviceName) throws DevFailed {
        super(deviceName);
        name = deviceName.substring(deviceName.lastIndexOf('/')+1);
        buildStateViewer(deviceName);
        compactionChart = new CompactionChart(this);
        compactionButton = new JRadioButton("Compaction");
        compactionButton.setFont(font);
        compactionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                compactionActionPerformed(evt);
            }
        });

        //  Subscribe to pipe event
        TangoEventsAdapter adapter = new TangoEventsAdapter(this);
        PipeEventListener pipeListener = new PipeEventListener();
        adapter.addTangoPipeListener(pipeListener, pipeName, TangoConst.NOT_STATELESS);

    }
    //===============================================================
    //===============================================================
    private void compactionActionPerformed(ActionEvent event) {
        //  Cancel action
        JRadioButton btn = (JRadioButton) event.getSource();
        btn.setSelected(!btn.isSelected());
    }
    //===============================================================
    //===============================================================
    public String getName() {
        return name;
    }
    //===============================================================
    //===============================================================
    public JRadioButton getCompactionButton() {
        return compactionButton;
    }
    //===============================================================
    //===============================================================
    public StateViewer getStateViewer() {
        return stateViewer;
    }
    //===============================================================
    //===============================================================
    public CompactionChart getCompactionChart() {
        return compactionChart;
    }
    //===============================================================
    //===============================================================
    public List<Compaction> getCompactionList() {
        return compactionList;
    }
    //===============================================================
    //===============================================================
    private void buildStateViewer(String deviceName) throws DevFailed {
        try {
            //  Add a state viewer
            stateViewer = new StateViewer();
            stateViewer.setFont(font);
            stateViewer.setLabel("");
            stateViewer.setStatePreferredSize(new Dimension(60, 15));
            //errorHistory.add(stateViewer);

            IDevStateScalar attState =
                    (IDevStateScalar) attributeList.add(deviceName + "/state");
            stateViewer.setModel(attState);
            //attState.addDevStateScalarListener(this);
            attState.refresh();
        }
        catch (ConnectionException e) {
            Except.throw_exception("ConnectionFailed", e.getDescription());
        }
    }
    //===============================================================
    //===============================================================
    private void buildCompactions(PipeBlob pipeBlob) {
        compactionList.clear();
        if (pipeBlob.getName().equals("Compactions")) {
            compactionButton.setSelected(true);
            for (PipeDataElement dataElement : pipeBlob) {
                compactionList.add(new Compaction(dataElement));
            }
        }
        else {
            compactionButton.setSelected(false);
        }

        //for (Compaction compaction : compactionList)
        //    System.out.println(compaction);

        //  Update chart
        if (compactionChart!=null)
            compactionChart.updateCurves();
    }
    //===============================================================
    //===============================================================
    public void resetSimulation() throws DevFailed {
        command_inout("ResetSimulation");
    }
    //===============================================================
    //===============================================================






    //=========================================================================
    /**
     * Compaction class
     */
    //=========================================================================
    class Compaction {
        String tableName;
        String taskName = "?";
        int    taskType = COMPACTION;
        long   total = -1;
        long   completed = -1;
        double ratio = 0.0;
        //=================================================================
        private Compaction(PipeDataElement pipeDataElement) {
            tableName = pipeDataElement.getName();

            PipeBlob pipeBlob = pipeDataElement.extractPipeBlob();
            taskName = pipeBlob.getName();
            if (taskName.equalsIgnoreCase("validation"))
                taskType = VALIDATION;

            for (PipeDataElement dataElement : pipeBlob) {
                String str = dataElement.getName();
                switch (str) {
                    case "total":
                        total = dataElement.extractLong64Array()[0];
                        break;
                    case "completed":
                        completed = dataElement.extractLong64Array()[0];
                        break;
                    case "ratio":
                        ratio = dataElement.extractDoubleArray()[0];
                        break;
                }
            }
        }
        //=================================================================
        public String toString() {
            return tableName + "(" + taskName + "): " +
                    completed + "/" +
                    total + " bytes\t"+ String.format("%6.2f", ratio*100) +"%";
        }
        //=================================================================
    }
    //=========================================================================
    //=========================================================================





    //=========================================================================
    /**
     * Pipe event listener
     */
    //=========================================================================
    public class PipeEventListener implements ITangoPipeListener {
        //=================================================================
        public void pipe(TangoPipeEvent event) {

            try {
                //	Get the attribute value
                DevicePipe devicePipe = event.getValue();
                buildCompactions(devicePipe.getPipeBlob());
            } catch (DevFailed e) {
                Except.print_exception(e);
                //System.err.println(e.errors[0].desc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //=====================================================================
    //=====================================================================
}