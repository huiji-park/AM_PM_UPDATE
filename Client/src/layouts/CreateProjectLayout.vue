<template>
  <Header class="create_project_header"/>
  <div class="main_container">
    <router-view />
  </div>
  <Footer class="create_project_footer" />
</template>

<script setup>
import Header from "@/components/Header.vue";
import Footer from "@/components/Footer.vue";
</script>
<style scoped>
@import "../assets/css/createProjectLayout.css";
</style>
