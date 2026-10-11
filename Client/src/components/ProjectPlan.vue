<template>
  <div class="project-plan" :key="pageId">

    <h1>{{ pageTitle }}</h1>
    <div>
      <p>참고 사이트: <a :href="sampleUrl" target="_blank" >{{ sampleUrl }}</a></p>
      <input type="text" placeholder="원하는 Url로 수정하세요!" v-model="editableSampleUrl" />
      <button class="b" @click="saveUrl">저장</button>
      <br><br>
      <input type="file" @change="handleFileUpload" />
      <button class="a" @click="uploadFile">파일 업로드</button><br><br>
      <br>
      <img :src="imagePreview" alt="Preview" v-if="imagePreview" height="800px" width="1200px"/>

      <v-container>
          <v-row align="end" justify="end">
          <v-col cols="auto">
            <v-btn @click="deletePage" icon="mdi-plus" size="small">X</v-btn>
          </v-col>
          </v-row>
      </v-container>

    </div>
    <Sidebar :currentProjectId="projectId" />
  </div>
</template>

<script>
import axios from 'axios';
import Swal from 'sweetalert2';

export default {
  props: {
    pageType: {
      type: String,
      required: true
    },
    projectId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      projectId: sessionStorage.getItem("projectId"),
      pages: [],
      sampleUrl: '',
      editableSampleUrl: '',
      imagePreview: null,
      uploadedFile: null,
    };
  },
  computed: {
    pageTitle() {
      const pageTypeTitle = `${this.pageType} Page`;
      return pageTypeTitle.toUpperCase();
    },
    uploadUrl() {
      const projectId = sessionStorage.getItem("projectId");
      return this.pageType ? `http://localhost:8090/api/plan/user-${this.pageType.toLowerCase()}/${projectId}` : '';
    },
    defaultData() {
    return {
      ERD: {
        sampleUrl: 'https://www.erdcloud.com/',
        imagePreview: 'http://localhost:8090/img/plan/default-erd-image.png'
      },
      USECASE: {
        sampleUrl: 'https://darw.io/',
        imagePreview: 'http://localhost:8090/img/plan/default-usecase-image.png'
      },
      ui: {
        sampleUrl: 'https://www.figma.com/',
        imagePreview: 'http://localhost:8090/img/plan/default-ui-image.png'
      }
    };
  }
  },
  watch: {
  '$route.params.pageType': function(newType, oldType) {
    if (newType !== oldType) {
      this.fetchPageData();
    }
  },
  '$route.params.pageId': function(newId, oldId) {
    if (newId !== oldId) {
      this.fetchPageData();
    }
  }
},

  methods: {
    deletePage() {
    const projectId = sessionStorage.getItem("projectId");
    const pageTitle = this.pageType;

    if (!pageTitle) {
        console.error('undefined');
        return;
    }

    axios.delete(`http://localhost:8090/api/plan/${pageTitle}`)
        .then(response => {
            Swal.fire({
                title: 'Success!',
                text: '페이지가 삭제되었습니다.',
                icon: 'success',
                confirmButtonText: 'OK'
            }).then((result) => {
                if (result.isConfirmed) {
                    this.$router.push(`/srs`);
                }
            });
        })
        .catch(error => {
            console.error('페이지 삭제 실패:', error);
            Swal.fire({
                title: 'Error!',
                text: error.response.data.message || '삭제 실패',
                icon: 'error',
                confirmButtonText: 'Close'
            });        
        });
},

    addEtcPage(newPageTitle) {
    this.etcPages.push({ title: newPageTitle, projectId: this.currentProjectId });
  },


  /* 사이드바에서 구현 
  createNewPage() {
    const projectId = this.$route.params.projectId;
    const formData = new FormData();
    formData.append('projectId', projectId);

    if (this.uploadedFile) {
      formData.append('file', this.uploadedFile);
    }

    const defaultDataForPageType = this.defaultData[this.pageType];
    if (!defaultDataForPageType) {
      console.error('Unknown page type:', this.pageType);
      return;
    }

  formData.append('sampleUrl', defaultDataForPageType.sampleUrl);
  formData.append('sampleImg', defaultDataForPageType.imagePreview);

  axios.post('http://localhost:8090/api/plan/create-page', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  .then(response => {
    const newPageData = response.data;
    if (Array.isArray(this.pages)) {
      this.pages.push(newPageData);
      this.$emit('add-etc-page', `ETC${this.pages.length}`);
    }
    alert(`새로운 페이지가 생성되었습니다: ${newPageData.title}`);
  })
  .catch(error => {
    console.error('페이지 생성 실패:', error);
  });
},
*/


saveUrl() {
  const projectId = sessionStorage.getItem("projectId");
  const title = this.pageType; 
  axios.put(`http://localhost:8090/api/plan/update-url/${projectId}/${title}`, { newSampleUrl: this.editableSampleUrl })
    .then(response => {
      Swal.fire({
      title: 'Success!',
      text: 'URL 수정 성공!',
      icon: 'success',
      confirmButtonText: 'OK'
    });
      this.sampleUrl = this.editableSampleUrl;
      console.log('URL 업데이트 성공:', response);
    })
    .catch(error => {
      console.error('URL 업데이트 실패:', error);
    });
},
    handleFileUpload(event) {
      const file = event.target.files[0];
      this.imagePreview = URL.createObjectURL(file);
      this.uploadedFile = file;
    },
    fetchPageData() {
  if (this.pageType) {
    const projectId = sessionStorage.getItem("projectId");
    if (this.pageType && projectId) {
      const url = `http://localhost:8090/api/plan/${this.pageType.toLowerCase()}-example/${projectId}`;
      axios.get(url)
        .then(response => {
          const data = response.data;
          this.sampleUrl = data.sampleUrl || this.defaultData.sampleUrl;
          this.imagePreview = data.filePath || data.sampleImg || this.defaultData.imagePreview;
        })
        .catch(error => {
          console.error('데이터를 가져오는 데 실패했습니다:', error);
        });
    }
  }
},

uploadFile() {
  if (!this.uploadUrl || !this.uploadedFile) return;

  const formData = new FormData();
  formData.append('file', this.uploadedFile);

  const title = this.pageType; 
  const uploadUrlWithTitle = `${this.uploadUrl}/${title}`;

  axios.post(uploadUrlWithTitle, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }).then(response => {
    Swal.fire({
      title: 'Success!',
      text: '이미지 업로드 성공!',
      icon: 'success',
      confirmButtonText: 'OK'
    });
    this.imagePreview = response.data.filePath;
    this.fetchPageData();
    console.log('이미지 업로드 성공:', response);
  }).catch(error => {
    console.error('이미지 업로드 실패:', error);
  });
},
    beforeRouteUpdate(to, from, next) {
      if (to.params.pageType !== from.params.pageType || to.params.projectId !== from.params.projectId) {
        this.fetchPageData();
      }
      next();
    },
  },
  
  mounted() {
    
    const projectId = sessionStorage.getItem("projectId");
  if (this.pageType && projectId) {
    this.fetchPageData();
    }
  }
}
</script>

<style scoped>
@import "../assets/css/ProjectPlanPage.css";
</style>