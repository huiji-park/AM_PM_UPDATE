<template>
  <v-container fluid class="excel_container">
    <v-row class="mt-3 mx-1">
      <v-col class="px-1" cols="12" xs="12" sm="12" md="12" lg="12">
        <div id="spreadsheet" ref="spreadsheet"></div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import "jspreadsheet-ce/dist/jspreadsheet.css";
import jspreadsheet from "jspreadsheet-ce";

export default {
  name: "Excel",
  data() {
    return {
      data: [
        [
          "ex) signup_001",
          "회원가입",
          "생성",
          "자체 회원가입",
          "회원가입을 진행한다",
          "상",
          "간편로그인",
        ],
        [],
        [],
        [],
        [],
        [],
        [],
        [],
        [],
        [],
        [],
        [],
        [],
      ],
    };
  },
  computed: {
    jExcelOptions() {
      return {
        data: this.data,
        columns: [
          { type: "text", title: "요구사항 ID", width: 100 },
          { type: "text", title: "대분류", width: 100, align: "center" },
          { type: "text", title: "소분류", width: 80, align: "center" },
          { type: "text", title: "이름", width: 180, align: "center" },
          { type: "text", title: "설명", width: 500, align: "center" },
          { type: "text", title: "우선순위", width: 70, align: "center" },
          { type: "text", title: "비고", width: 300, align: "center" },
        ],
      };
    },
  },
  mounted() {
    this.jExcelObj = jspreadsheet(this.$refs.spreadsheet, this.jExcelOptions);
  },
};
</script>

<style scoped>
@import "../../assets/css/excel.css";
</style>

