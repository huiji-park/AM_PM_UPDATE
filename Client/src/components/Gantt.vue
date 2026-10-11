<template>
  <div v-if="!isProjectDateVailed" class="gantt_container">
    <h1>Gantt Chart</h1>
    <p class="gantt_error_text">프로젝트 기간을 선택해주세요.</p>
  </div>
  <div v-if="isProjectDateVailed" class="gantt_container">
    <h1>Gantt Chart</h1>
    <div class="gantt_item_container">
      <g-gantt-chart
        :chart-start="projectStartDate"
        :chart-end="projectEndDate"
        bar-start="myBeginDate"
        bar-end="myEndDate"
      >
        <g-gantt-row
          v-for="(bars, index) in barsList"
          :key="index"
          :label="`${index + 1}`"
          :bars="bars"
          @click="handleRowClick(index)"
        />
      </g-gantt-chart>
    </div>
    <div class="gantt_addTask_container">
      <input v-model="addData" type="text" />
      <button @click="addTaskHandler">Add Task</button>
    </div>
    <div v-if="taskEditor" class="gantt_edit_container">
      <h1>Task Edit</h1>
      <div class="select_task_container">
        <p>선택된 Task :{{ editData.taskIndex }}</p>
        <button @click="closeEditor">닫기</button>
      </div>
      <div class="gantt_edit_wrapper">
        <input v-model="editData.context" type="text" />
        <button @click="deleteTaskHandler" class="bg_red">삭제</button>
        <button @click="editTaskHandler" class="bg_yellow">수정 적용</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { expireToken } from "../api/config";
import {
  getProject,
  getTasks,
  addTasks,
  deleteTask,
  editTask,
} from "../api/common";
import Swal from "sweetalert2";

const projectStartDate = ref("2021-07-11 12:00");
const projectEndDate = ref("2021-07-24 12:00");
const projectId = sessionStorage.getItem("projectId");
const isProjectDateVailed = ref(true);

const barsList = ref([]);
const addData = ref("");
const taskEditor = ref(false);
const editData = ref({
  taskIndex: 0,
  context: "",
  taskId: "",
});

onMounted(() => {
  getProjectDate();
});

const Toast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 2000,
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.addEventListener("mouseenter", Swal.stopTimer);
    toast.addEventListener("mouseleave", Swal.resumeTimer);
  },
});

const editTaskHandler = (event) => {
  event.stopPropagation();
  const index = editData.value.taskIndex - 1;
  const beginDate = barsList._rawValue[index][0].myBeginDate;
  const endDate = barsList._rawValue[index][0].myEndDate;

  const data = {
    beginDate: formatDateEdit(beginDate),
    endDate: formatDateEdit(endDate),
    content: editData.value.context,
  };

  editTask(editData.value.taskId, data)
    .then(() => {
      Toast.fire({
        icon: "success",
        title: "Task 수정 완료",
      });
      getTasksHandler(projectId);
      window.scrollTo(0, 0);
    })
    .catch((err) => {
      console.log(err);
    });
};

const deleteTaskHandler = (event) => {
  event.stopPropagation();
  deleteTask(editData.value.taskId)
    .then(() => {
      Toast.fire({
        icon: "success",
        title: "Task 삭제 완료",
      });
      getTasksHandler(projectId);
      closeEditor();
    })
    .catch((err) => {
      console.log(err);
    });
};

const closeEditor = () => {
  taskEditor.value = false;
};

const handleRowClick = (index) => {
  taskEditor.value = true;
  editData.value.taskIndex = index + 1;
  const content = barsList._rawValue[index][0].ganttBarConfig.label;
  editData.value.context = content;
  const taskId = barsList._rawValue[index][0].ganttBarConfig.id;
  editData.value.taskId = taskId;
};

const addTaskHandler = () => {
  console.log(addData.value);
  const data = {
    projectId,
    content: addData.value,
  };
  addTasks(data)
    .then((res) => {
      Toast.fire({
        icon: "success",
        title: "Task 추가 완료",
      });
      getTasksHandler(projectId);
      closeEditor();
    })
    .catch((err) => {
      expireToken(err, addTaskHandler);
    });
};

const getProjectDate = () => {
  getProject(projectId)
    .then((res) => {
      return res.data;
    })
    .then((data) => {
      const { startDate, endDate } = data;
      projectStartDate.value = startDate + " 00:00";
      projectEndDate.value = endDate + " 00:00";
      if (
        projectStartDate.value === "null 00:00" ||
        projectEndDate.value === "null 00:00"
      ) {
        isProjectDateVailed.value = false;
      }
    })
    .then(() => {
      getTasksHandler(projectId);
    })
    .catch((err) => console.log(err));
};

const getTasksHandler = (projectId) => {
  getTasks(projectId)
    .then((res) => {
      return res.data;
    })
    .then((data) => {
      barsList.value = [];
      data.forEach((item, index) => {
        barsList.value.push(itemToBars(item, index + 1));
      });
      return barsList;
    });
};

function itemToBars(item) {
  return [
    {
      nickname: item.nickname,
      myBeginDate: formatDate(item.beginDate),
      myEndDate: formatDate(item.endDate),
      ganttBarConfig: {
        hasHandles: true,
        id: item.id,
        label: item.content,
        style: {
          background: item.backgroundColor,
          color: item.frontColor,
        },
      },
    },
  ];
}

function formatDate(inputDate) {
  const date = new Date(inputDate);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0"); // 월은 0부터 시작하므로 +1 필요
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  return `${year}-${month}-${day} ${hours}:${minutes}`;
}

function formatDateEdit(inputDateString) {
  const [datePart, timePart] = inputDateString.split(" ");

  const iso8601DateString = `${datePart}T${timePart}:00`;

  return iso8601DateString;
}
</script>
<style scoped>
@import "../assets/css/gantt.css";
</style>
