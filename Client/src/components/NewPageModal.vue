<template>
  <v-dialog :value="modalVisible" @input="$emit('update:modalVisible', $event)">
    <v-card>
      <v-card-title>새 페이지 만들기</v-card-title>
      <v-card-text>
        <v-text-field
          label="페이지 제목"
          v-model="newPageTitle"
          :rules="[rules.required, rules.isEnglish]"
        ></v-text-field>
      </v-card-text>
      <v-card-actions>
        <v-btn text @click="closeModal">취소</v-btn>
        <v-btn color="primary" @click="createPage">생성</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    modalVisible: Boolean
  },

  data() {
    return {
      newPageTitle: ''
    };
  },
  computed: {
    rules() {
      return {
        required: value => !!value || '제목을 입력해주세요.',
        isEnglish: value => /^[A-Za-z]+$/.test(value) || '영어로만 입력해주세요.'
      };
    }
  },
  methods: {
    closeModal() {
      this.$emit('update:modalVisible', false);
      this.newPageTitle = '';
    },
    async createPage() {
      // 페이지 생성 로직
      this.closeModal();
      // 페이지 이동 로직
    }
    // 기타 메소드
  }
};
</script>

<style>
@import "../assets/css/modal.css";
</style>
