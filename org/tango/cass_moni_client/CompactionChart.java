//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009,2010,2011,2012,2013,2014,2015
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
// $Revision:  $
//
// $Log:  $
//
//-======================================================================

package org.tango.cass_moni_client ;

import fr.esrf.tangoatk.widget.util.chart.*;

import java.awt.*;
import java.util.List;

import static org.tango.cass_moni_client.CassandraNode.COMPACTION;
import static org.tango.cass_moni_client.CassandraNode.VALIDATION;


//===============================================================
/**
 *	JDialog Class to display info
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class CompactionChart extends JLChart implements IJLChartListener {
	private CassandraNode cassandraNode;
    private JLDataView compactionDataView;
    private JLDataView validationDataView;
    private static final Dimension chartDimension = new Dimension(300, 250);
	//===============================================================
	//===============================================================
	public CompactionChart(CassandraNode cassandraNode) {
		super();
		this.cassandraNode = cassandraNode;
		setHeaderFont(new Font("Dialog", Font.BOLD, 14));
		setHeader(cassandraNode.getName());
		setLabelVisible(false);
		//setLabelPlacement(JLChart.LABEL_ROW);
		setLabelFont(new Font("Dialog", Font.BOLD, 12));
		setJLChartListener(this);

		//  Set Y axis.
		JLAxis y1Axis = getY1Axis();
		y1Axis.setName("Compaction (%)");
		y1Axis.setAutoScale(false);
        y1Axis.setMaximum(100);
		y1Axis.setGridVisible(true);

		//  Set X axis.
		JLAxis  xAxis = getXAxis();
		xAxis.setName("table in compaction");
		xAxis.setAnnotation(JLAxis.VALUE_ANNO);
		xAxis.setGridVisible(true);
		xAxis.setSubGridVisible(true);
        xAxis.setMinimum(0);
        xAxis.setAutoScale(false);

        compactionDataView = buildDataView("Compactions", Color.red, getY1Axis());
        validationDataView = buildDataView("Validations", new Color(0x009900), getY1Axis());
        setPreferredSize(chartDimension);
	}
	//===============================================================
	//===============================================================
    private JLDataView buildDataView(String name, Color color, JLAxis axis) {
        JLDataView dataView = new JLDataView();
        dataView.setName(name);
        dataView.setColor(color);
        dataView.setFill(true);
        dataView.setFillColor(color);
        dataView.setViewType(JLDataView.TYPE_BAR);

        axis.addDataView(dataView);
        return dataView;
    }
	//===============================================================
	//===============================================================
    public void updateCurves() {
        compactionDataView.reset();
        validationDataView.reset();
        List<CassandraNode.Compaction> compactionList = cassandraNode.getCompactionList();
        if (compactionList.isEmpty()) {
            setHeader(cassandraNode.getName() + " (No Compaction)");
        }
        else {
            setHeader(cassandraNode.getName());
            int x = 1;
            for (CassandraNode.Compaction compaction : compactionList) {
                if (compaction.taskType == COMPACTION)
                    compactionDataView.add(x++, 100.0 * compaction.ratio);
                else
                    validationDataView.add(x++, 100.0 * compaction.ratio);
            }
            getXAxis().setMaximum(compactionList.size() + 1);
        }
        repaint();
    }
    //===============================================================
    //===============================================================
    @Override
    public String[] clickOnChart(JLChartEvent chartEvent) {
        JLDataView dataView = chartEvent.getDataView();
        int index = chartEvent.getDataViewIndex();
        List<CassandraNode.Compaction> compactionList = cassandraNode.getCompactionList();
        CompactionChartDialog.setSelection(cassandraNode);

        int i=0;
        for (CassandraNode.Compaction compaction : compactionList) {
            //  Search index for specified data view
            if ((dataView == compactionDataView && compaction.taskType == COMPACTION) ||
                (dataView == validationDataView && compaction.taskType == VALIDATION)) {

                //  If found, return compaction/validation parameters
                if (i == index) {
                    return new String[]  { compaction.tableName };
                }
                i++;
            }
        }

        return new String[] { "index " + index + " not found in " + dataView.getName()};
    }
    //===============================================================
	//===============================================================
}
