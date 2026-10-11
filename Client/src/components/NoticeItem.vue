<template>
  <div @click="openDetailModalHandler" class="notice_item_container">
    <p>{{ noticeItem.title }}</p>
  </div>
</template>

<script setup>
import { defineProps } from "vue";
const props = defineProps(["noticeItem", "openDetailModal"]);

const openDetailModalHandler = () => {
  props.openDetailModal(props.noticeItem);
};
</script>
<style scoped>
@import "../assets/css/notice.css";
</style>
