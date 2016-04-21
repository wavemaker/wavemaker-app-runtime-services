/**
 * Copyright © 2013 - 2016 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.util;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.wavemaker.runtime.data.model.CustomProcedureParam;
import com.wavemaker.runtime.data.model.ProcedureParamType;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * Created by anitha on 2/11/15.
 */

@Test
public class ProceduresUtilsTest {


    public void hasOutParamTest() {
        ProceduresUtils p = new ProceduresUtils();
        CustomProcedureParam cmp1 = new CustomProcedureParam("example", p, ProcedureParamType.OUT, "int");
        CustomProcedureParam cmp2 = new CustomProcedureParam("Sample", p, ProcedureParamType.IN, "int");
        CustomProcedureParam cmp3 = new CustomProcedureParam("test", p, ProcedureParamType.IN_OUT, "int");
        CustomProcedureParam cmp4= new CustomProcedureParam("Checking",p,ProcedureParamType.IN,"int");
        List<CustomProcedureParam> customProcedureParamList= new ArrayList<>();
        List<CustomProcedureParam> customProcedureParamList1= new ArrayList<>();
        List<CustomProcedureParam> customProcedureParamList2= new ArrayList<>();
        customProcedureParamList.add(cmp1);
        customProcedureParamList.add(cmp2);
        customProcedureParamList1.add(cmp2);
        customProcedureParamList1.add(cmp4);
        customProcedureParamList2.add(cmp3);
        assertTrue(ProceduresUtils.hasOutParam(customProcedureParamList));
        assertFalse(ProceduresUtils.hasOutParam(customProcedureParamList1));
        assertTrue(ProceduresUtils.hasOutParam(customProcedureParamList2));
        assertTrue(ProceduresUtils.hasOutParam(customProcedureParamList));
        assertFalse(ProceduresUtils.hasOutParam(customProcedureParamList1));

    }

    public void hasOutParamTypeTest(){
        ProceduresUtils p = new ProceduresUtils();
        CustomProcedureParam cmp1= new CustomProcedureParam("example",p,ProcedureParamType.OUT,"int");
        assertTrue(ProceduresUtils.hasOutParamType(cmp1));
        CustomProcedureParam cmp2= new CustomProcedureParam("sample",p,ProcedureParamType.IN,"int");
        assertFalse(ProceduresUtils.hasOutParamType(cmp2));
        CustomProcedureParam cmp3= new CustomProcedureParam("example",p,ProcedureParamType.IN_OUT,"int");
        assertTrue(ProceduresUtils.hasOutParamType(cmp3));

    }

    public void jdbcComplianceProcedure()
    {
        final String procedure1 = "sp_create_workorder_detail\n" +
                ":workorder,\n" +
                ":workorderType,\n" +
                ":typeid,\n" +
                ":workorder1,\n";
        final String jdbcComplianceProcedure1 = "sp_create_workorder_detail\n" +
                "?,\n" +
                "?,\n" +
                "?,\n" +
                "?,\n";

        final String procedure2 = "sp_create_workorder_detail    :workorder,:workorderType,:typeid,:workorder1";
        final String jdbcComplianceProcedure2 = "sp_create_workorder_detail    ?,?,?,?";

        final String procedure3 = "sp_create_workorder_detail :workorder   ,:workorderType,:typeid,   :workorder1";
        final String jdbcComplianceProcedure3 ="sp_create_workorder_detail ?,?,?,   ?";

        final String procedure4 = "sp_create_workorder_detail :workorderType,:typeid,   :workorder1,:workorder   ";
        final String jdbcComplianceProcedure4= "sp_create_workorder_detail ?,?,   ?,?";

        final String procedure5 = "sp_create_workorder_detail :workorderType,:workorder  ,:typeid,   :workorder1";
        final String jdbcComplianceProcedure5= "sp_create_workorder_detail ?,?,?,   ?";

        final String procedure6 = "sp_create_workorder_detail :workorderType,:workorder,:typeid,   :workorder1";
        final String jdbcComplianceProcedure6= "sp_create_workorder_detail ?,?,?,   ?";

        final String procedure7 = "sp_create_workorder_detail (:workorder)";
        final String jdbcComplianceProcedure7= "sp_create_workorder_detail (?)";

        final String procedure8 = "sp_create_workorder_detail (:workordertype,:workorder   , :workorderNo)";
        final String jdbcComplianceProcedure8= "sp_create_workorder_detail (:workordertype,?, :workorderNo)";

        final String namedParam [] = {"workorder","workorderType","typeid","workorder1"};

        Assert.assertEquals(jdbcComplianceProcedure1,ProceduresUtils.jdbcComplianceProcedure(procedure1,namedParam));
        Assert.assertEquals(jdbcComplianceProcedure2,ProceduresUtils.jdbcComplianceProcedure(procedure2,namedParam));
        Assert.assertEquals(jdbcComplianceProcedure3,ProceduresUtils.jdbcComplianceProcedure(procedure3,namedParam));
        Assert.assertEquals(jdbcComplianceProcedure4,ProceduresUtils.jdbcComplianceProcedure(procedure4,namedParam));
        Assert.assertEquals(jdbcComplianceProcedure5,ProceduresUtils.jdbcComplianceProcedure(procedure5,namedParam));
        Assert.assertEquals(jdbcComplianceProcedure6,ProceduresUtils.jdbcComplianceProcedure(procedure6,namedParam));
        Assert.assertEquals(jdbcComplianceProcedure7,ProceduresUtils.jdbcComplianceProcedure(procedure7,namedParam));
        Assert.assertEquals(jdbcComplianceProcedure8,ProceduresUtils.jdbcComplianceProcedure(procedure8,namedParam));


    }

    public static void main(String[] args) {
        new ProceduresUtilsTest().jdbcComplianceProcedure();
    }

}















