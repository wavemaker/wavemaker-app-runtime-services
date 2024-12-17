/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wavemaker.runtime.data.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.wavemaker.runtime.data.model.CustomProcedureParam;
import com.wavemaker.runtime.data.model.procedures.ProcedureParameterType;

/**
 * Created by anitha on 2/11/15.
 */

@Test
public class ProceduresUtilsTest {

    public void hasOutParamTest() {
        ProceduresUtils p = new ProceduresUtils();
        CustomProcedureParam cmp1 = new CustomProcedureParam("example", p, ProcedureParameterType.OUT, "int");
        CustomProcedureParam cmp2 = new CustomProcedureParam("Sample", p, ProcedureParameterType.IN, "int");
        CustomProcedureParam cmp3 = new CustomProcedureParam("test", p, ProcedureParameterType.IN_OUT, "int");
        CustomProcedureParam cmp4 = new CustomProcedureParam("Checking", p, ProcedureParameterType.IN, "int");
        List<CustomProcedureParam> customProcedureParamList = new ArrayList<>();
        List<CustomProcedureParam> customProcedureParamList1 = new ArrayList<>();
        List<CustomProcedureParam> customProcedureParamList2 = new ArrayList<>();
        customProcedureParamList.add(cmp1);
        customProcedureParamList.add(cmp2);
        customProcedureParamList1.add(cmp2);
        customProcedureParamList1.add(cmp4);
        customProcedureParamList2.add(cmp3);
        Assert.assertTrue(ProceduresUtils.hasOutParam(customProcedureParamList));
        Assert.assertFalse(ProceduresUtils.hasOutParam(customProcedureParamList1));
        Assert.assertTrue(ProceduresUtils.hasOutParam(customProcedureParamList2));
        Assert.assertTrue(ProceduresUtils.hasOutParam(customProcedureParamList));
        Assert.assertFalse(ProceduresUtils.hasOutParam(customProcedureParamList1));

    }

    public void hasOutParamTypeTest() {
        ProceduresUtils p = new ProceduresUtils();
        CustomProcedureParam cmp1 = new CustomProcedureParam("example", p, ProcedureParameterType.OUT, "int");
        Assert.assertTrue(cmp1.getProcedureParamType().isOutParam());
        CustomProcedureParam cmp2 = new CustomProcedureParam("sample", p, ProcedureParameterType.IN, "int");
        Assert.assertFalse(cmp2.getProcedureParamType().isOutParam());
        CustomProcedureParam cmp3 = new CustomProcedureParam("example", p, ProcedureParameterType.IN_OUT, "int");
        Assert.assertTrue(cmp3.getProcedureParamType().isOutParam());

    }

    public void jdbcComplianceProcedure() {
        final String procedure1 = """
            sp_create_workorder_detail
            :workorder,
            :workorderType,
            :typeid,
            :workorder1,
            """;
        final String jdbcComplianceProcedure1 = """
            sp_create_workorder_detail
            ?,
            ?,
            ?,
            ?,
            """;

        final String procedure2 = "sp_create_workorder_detail    :workorder,:workorderType,:typeid,:workorder1";
        final String jdbcComplianceProcedure2 = "sp_create_workorder_detail    ?,?,?,?";

        final String procedure3 = "sp_create_workorder_detail :workorder   ,:workorderType,:typeid,   :workorder1";
        final String jdbcComplianceProcedure3 = "sp_create_workorder_detail ?   ,?,?,   ?";

        final String procedure4 = "sp_create_workorder_detail :workorderType,:typeid,   :workorder1,:workorder   ";
        final String jdbcComplianceProcedure4 = "sp_create_workorder_detail ?,?,   ?,?   ";

        final String procedure5 = "sp_create_workorder_detail :workorderType,:workorder  ,:typeid,   :workorder1";
        final String jdbcComplianceProcedure5 = "sp_create_workorder_detail ?,?  ,?,   ?";

        final String procedure6 = "sp_create_workorder_detail :workorderType,:workorder,:typeid,   :workorder1";
        final String jdbcComplianceProcedure6 = "sp_create_workorder_detail ?,?,?,   ?";

        final String procedure7 = "sp_create_workorder_detail (:workorder)";
        final String jdbcComplianceProcedure7 = "sp_create_workorder_detail (?)";

        final String procedure8 = "sp_create_workorder_detail (:workordertype,:workorder   , :workorderNo)";
        final String jdbcComplianceProcedure8 = "sp_create_workorder_detail (:workordertype,?   , :workorderNo)";

        final String procedure9 = "update Register set name=:workorder where city=:workorderType and age =:typeid";
        final String jdbcComplianceProcedure9 = "update Register set name=? where city=? and age =?";

        final String[] namedParams = {"workorder", "workorderType", "typeid", "workorder1"};

        final Set<String> namedParamSet = new HashSet<>(Arrays.asList(namedParams));

        Assert.assertEquals(jdbcComplianceProcedure1, ProceduresUtils.jdbcComplianceProcedure(procedure1, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure2, ProceduresUtils.jdbcComplianceProcedure(procedure2, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure3, ProceduresUtils.jdbcComplianceProcedure(procedure3, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure4, ProceduresUtils.jdbcComplianceProcedure(procedure4, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure5, ProceduresUtils.jdbcComplianceProcedure(procedure5, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure6, ProceduresUtils.jdbcComplianceProcedure(procedure6, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure7, ProceduresUtils.jdbcComplianceProcedure(procedure7, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure8, ProceduresUtils.jdbcComplianceProcedure(procedure8, namedParamSet));
        Assert.assertEquals(jdbcComplianceProcedure9, ProceduresUtils.jdbcComplianceProcedure(procedure9, namedParamSet));

    }

    public static void main(String[] args) {
        new ProceduresUtilsTest().jdbcComplianceProcedure();
    }

}















