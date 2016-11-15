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

package org.tango.cassandramonitor;

import fr.esrf.TangoApi.PipeBlob;
import fr.esrf.TangoApi.PipeDataElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This class is able to convert a JMX attribute
 * (list of map) to a PipeBlob with compaction objects
 *
 * @author verdier
 */

public class JmxToCompactions {
    private List<Compaction> previousCompactionList = new ArrayList<>();
    private List<Compaction> compactionList = new ArrayList<>();
    private boolean changed;

    //===============================================================
    //===============================================================
    JmxToCompactions() {
    }
    //===============================================================
    //===============================================================
    public void setList(List<HashMap<String, String>> list) {
        compactionList = new ArrayList<>();
        for (HashMap<String, String> map : list) {
            Compaction compaction = new Compaction(map);
            compactionList.add(compaction);
            System.out.println(compaction);
        }
        checkIfChanged();
    }
    //===============================================================
    /*
     * A constructor to simulate
     */
    //===============================================================
    private static final int total = 1000000;
    private static final int total2 = 400000;
    private static final int defaultValue = 100000;
    private int completed = defaultValue;
    private int completed2 = defaultValue ;
    public void setList() {
        compactionList = new ArrayList<>();
        if (completed<=total) {
            compactionList.add(new Compaction("Compaction", "hdb.table_1", "" + total, "" + completed));
            compactionList.add(new Compaction("Compaction", "hdb.table_2", "6000000", "3000000"));
            compactionList.add(new Compaction("Compaction", "hdb.table_3", "5000000", "2000000"));
            completed += 10000;
            //  Simulate a validation faster
            if (completed2<total2) {
                compactionList.add(new Compaction("Validation", "hdb.table_4", "" + total2, "" + completed2));
                completed2 += 10000;
            }
        }
        /*
        else {
            // list will be empty -> no compaction
        }
        */
        checkIfChanged();
    }
    //===============================================================
    //===============================================================
    void reset() {
        completed = defaultValue;
        completed2 = defaultValue;
    }
    //===============================================================
    //===============================================================
    private void checkIfChanged() {
        changed = false;
        if (compactionList.size()!=previousCompactionList.size()) {
            changed = true;
        }
        else {
            //  List have same size -> compare each element
            for (int i=0 ; i<compactionList.size() ; i++) {
                if (compactionList.get(i).different(previousCompactionList.get(i))) {
                    changed = true;
                }
            }
        }
        if (changed) {
            previousCompactionList = compactionList;
        }
    }
    //===============================================================
    public boolean hasChanged() {
        return changed;
    }
    //===============================================================
    public PipeBlob getPipeBlob() {
        PipeBlob pipeBlob;
        //  Build the pipe content
        if (compactionList.isEmpty()) {
            pipeBlob = new PipeBlob("NO Compaction");
        }
        else {
            pipeBlob = new PipeBlob("Compactions");
            for (Compaction compaction : compactionList) {
                pipeBlob.add(compaction.getPipeDataElement());
            }
        }
        return pipeBlob;
    }
    //===============================================================
    //===============================================================



    //===============================================================
    /**
     * This class is able to define a compaction object
     *
     * @author verdier
     */
    //===============================================================
    public class Compaction {
        private String taskType;
        private String tableName;
        private long total;
        private long completed;
        private double ratio = 0.0;
        //===============================================================
        public Compaction(HashMap<String, String>  map) {
            taskType = map.get("taskType");
            total = Long.parseLong(map.get("total"));
            completed = Long.parseLong(map.get("completed"));
            total = Long.parseLong(map.get("total"));
            completed = Long.parseLong(map.get("completed"));
            if (total!=0)
                ratio = (double)completed/total;
            String keyspace = map.get("keyspace");
            String columnFamily = map.get("columnfamily");
            tableName = keyspace+"."+columnFamily;
        }
        //===============================================================
        Compaction(String taskType, String tableName, String total, String completed) {
            this.taskType = taskType;
            this.tableName = tableName;
            this.total = Long.parseLong(total);
            this.completed = Long.parseLong(completed);
            ratio = (double)this.completed/this.total;
        }
        //===============================================================
        public PipeDataElement getPipeDataElement() {
            PipeBlob pipeBlob = new PipeBlob(taskType);
            pipeBlob.add(new PipeDataElement("total", total));
            pipeBlob.add(new PipeDataElement("completed", completed));
            pipeBlob.add(new PipeDataElement("ratio", ratio));
            return new PipeDataElement(tableName, pipeBlob);
        }
        //===============================================================
        public boolean different(Compaction compaction) {
            return !taskType.equals(compaction.taskType)   ||
                   !tableName.equals(compaction.tableName) ||
                   total!=compaction.total || completed!=compaction.completed;
        }
        //===============================================================
        public String toString() {
            return "taskType=" + taskType + "  " +
                    "table=" + tableName + "  " +
                    "total=" + total + "  " +
                    "completed=" + completed + "  " + ratio;
        }
        //===============================================================
    }
}
