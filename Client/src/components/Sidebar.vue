<template>
    <div v-if="modalData != null" class="modal">
    <span class="xButton" @click="close">X</span>
    <div class="imgDiv">
      <img v-if="modalData.profileImg" :src="modalData.profileImg">
    </div>
    <div class="textDiv">
      <span class="nicknameModal">{{ modalData.nickname }}</span>
      <span class="emailModal">{{ modalData.email }}</span>
    </div>
    <div class="routerDiv">
      <RouterLink class="router" :to="{name: 'Chat', query: {user: modalData.userId}}">정보 확인</RouterLink>
    </div>
  </div>
  <v-card>
    <v-layout>
      <v-navigation-drawer id="sidebar_container" permanent>
        <p class="p">기획</p>
        <v-list density="compact" nav>
          
          <!-- 
          <v-list-item
            prepend-icon="mdi-home-city"
            title="요구사항 명세서"
            :to="'/srs'"

            style="color: white"
          ></v-list-item>
          
      <v-list-item
            prepend-icon="mdi-account-group-outline"
            title="USECASE"
            :to="'/usecase'"
            style="color: white"
      ></v-list-item>


        <v-list-item
            prepend-icon="mdi-account-group-outline"
            title="ERD"
            :to="'/erd'"
            style="color: white"
      ></v-list-item>

          <v-list-item
            prepend-icon="mdi-account-group-outline"
            title="UI"
            :to="'/ui'"
            style="color: white"
          ></v-list-item>
-->
          
          <v-list-item
            prepend-icon="mdi-account-group-outline"
            v-for="page in etcPages"
            :key="page.id"
            :title="page.title"
            :to="`/${page.title}`"
            style="color: white">
            <!-- 페이지 제목 또는 기타 정보 표시 -->
          </v-list-item>

<!--
        <v-list-item
          v-for="page in etcPages"
          :key="page.id"
          prepend-icon="mdi-file-document-outline"
          :title="page.title"
          :to="`/${page.title}/${currentProjectId}`"
          style="color: white"
        ></v-list-item>
-->

        </v-list>
        <v-container>
          <v-row align="end" justify="end">
          <v-col cols="auto">
        <v-btn @click="createNewEtcPage" icon="mdi-plus" size="small">+</v-btn>
          </v-col>
          </v-row>
      </v-container>
        <v-divider
          style="border-top-color: white; border-top-width: 2px"
        >
      </v-divider>
        
        
        <p class="p">개발</p>
        <v-list density="compact" nav>
          <v-list-item
            prepend-icon="mdi-home-city"
            title="공지사항/간트차트"
            to="/dev" 
            style="color: white"
          ></v-list-item>
          <v-list-item
            prepend-icon="mdi-account"
            title="채팅"
            to="/team-chat"
            style="color: white"
          ></v-list-item>
        </v-list>
        <v-divider
          style="border-top-color: white; border-top-width: 2px"
        ></v-divider>

        <p class="p">멤버</p>
        <v-list density="compact" nav >
          <v-list-item
            prepend-icon="mdi-account"
            :title= item.nickName
            :value= item.nickName
            style="color: white"
            v-for="item in member_list" :key="item.id" @click="show(item.userId)"></v-list-item>
          <!-- <v-list-item
            prepend-icon="mdi-account"
            title="민재"
            value="민재"
            style="color: white"
          ></v-list-item>
          -->
          <v-divider
          style="border-top-color: white; border-top-width: 2px"
        ></v-divider>     
         </v-list>
        
        <p class="p">관리</p>
        <v-list density="compact" nav>       
        <v-list-item
            prepend-icon="mdi-account"
            title="프로젝트 수정"
            to="/modify-project" 
            style="color: white"
          ></v-list-item>
          <v-list-item
            prepend-icon="mdi-account"
            title="멤버 관리"
            style="color: white"
            to="/member-list" 
          ></v-list-item>

        </v-list>
        <v-divider
          style="border-top-color: white; border-top-width: 2px"
        ></v-divider>
        <v-list density="compact" nav>
          <v-list-item
            prepend-icon="mdi-account"
            title="문의하기"
            to="question"
            style="color: white"
          ></v-list-item>
          </v-list>


        <v-list-item nav>
          <template v-slot:append>
            <v-btn variant="text" icon="mdi-chevron-left"></v-btn>
          </template>
        </v-list-item>
      </v-navigation-drawer>
    </v-layout>
  </v-card>

  <v-dialog v-model="showModal" persistent max-width="300px">
    <v-card>
      <v-card-title>새 페이지 생성</v-card-title>
      <v-card-text>
        <v-text-field 
        v-model="newPageTitle"
        label="페이지 제목"
        :rules="titleRules"
        ref="pageForm">
      </v-text-field>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue darken-1" text @click="handleCreatePage">생성</v-btn>
        <v-btn color="grey" text @click="showModal = false">취소</v-btn>



      </v-card-actions>
    </v-card>
  </v-dialog>

</template>

<script>

import axios from 'axios';
export default {
  watch: {
    etcPages(newVal, oldVal) {
      console.log('etcPages 변경됨:', newVal);
    }
  },
  props: {
    currentProjectId: {
    type: Number,
    required: false, 
  }
  },
  data() {
    return {
      drawer: true,
      rail: true,
      etcPages: [],
      showModal: false,
      newPageTitle: '',
      titleRules: [
        v => !!v || '제목은 필수입니다',
        v => /^[a-zA-Z\s]*$/.test(v) || '영어 제목만 가능합니다'
      ]

    };
  },
  methods: {
     
    getPageLink(title) {
      const projectId = sessionStorage.getItem("projectId");
      return `/${title}`;
    },
/*
    loadPages() {
      const projectId = sessionStorage.getItem("projectId");
      // 데이터가 이미 로드되었는지 확인
      if (this.etcPages.length === 0) {
        axios.get(`http://localhost:8090/api/plan/etc-pages/${projectId}`)
          .then(response => {
            this.etcPages = response.data;
            console.log("15151515")
          })
          .catch(error => {
            console.error("페이지 로드 실패:", error);
          });
      }
    },
*/

    createNewEtcPage() {
    this.showModal = true;
  },

  createPage() {
  if (this.$refs.pageForm.validate()) {
    this.createNewPage(this.newPageTitle).then(response => {
      this.showModal = false; 
      this.newPageTitle = ''; 
    }).catch(error => {
      console.error('Page creation failed:', error);
    });
  } else {
    console.log('유효하지 않은 제목');
  }
},
    
    requestProjectIdUpdate(newProjectId) {
      this.$emit('update-project-id', newProjectId);
    },

    addEtcPage(newPageTitle) {
        this.createNewPage(newPageTitle).then(response => {
            if (response && response.data) {
                this.etcPages = [...this.etcPages, response.data];
                // 새 페이지로 이동
                //this.$router.push(`/${response.data.title}/${this.currentProjectId}`);
            }
        }).catch(error => {
            console.error('Page creation failed:', error);
        });
  },

  /*
  createNewPage(title) {
  const projectId = sessionStorage.getItem("projectId");
  return axios.post('http://localhost:8090/api/plan/create-page', {
    title: title,
    projectId: projectId
  }).then(response => {
    const newPage = response.data;
    this.etcPages.push(newPage); 
  }).catch(error => {
    console.error('Page creation failed:', error);
    alert(error.response.data.message || '페이지 생성 실패');
  });
},
*/
  mounted() {
    this.loadPages();
  },

  computed: {
    isPlanningVisible() {
      return this.currentProjectId || this.etcPages.length > 0;
    }
  }
}
}
</script>

<style scoped>
@import "../assets/css/sidebar.css";
</style>

<script setup>
import { ref, onMounted } from "vue";
import { expireToken } from "../api/config";
import axios from "axios";
const drawer = ref(true);
const rail = ref(true);
const projectId = sessionStorage.getItem("projectId");
const modalData = ref(null);
const newPageTitle = ref('');



const etcPages = ref([]);
const loadPages = () => {
  axios.get(`http://localhost:8090/api/plan/etc-pages/` + projectId)
    .then(response => {
      etcPages.value = response.data;
    })
    .catch(err => {
      console.error("페이지 로드 실패:", err);
    }); 
};

const handleCreatePage = () => {
  if (newPageTitle.value.trim()) {
    createNewPageAndUpdateSidebar(newPageTitle.value.trim()).then(() => {
      // 페이지 생성 후 모달 닫기 
      showModal.value = false;
      newPageTitle.value = ''; // 입력 필드 초기화
    });
  }
};

const createNewPageAndUpdateSidebar = async (title) => {
  try {
    const response = await axios.post('http://localhost:8090/api/plan/create-page', {
      title: title,
      projectId: projectId
    });
    
    // 새 페이지를 etcPages에 추가
    etcPages.value.push(response.data);
    // 사이드바 데이터를 다시 로드
    await loadPages();
    
  } catch (error) {
    console.error('Page creation failed:', error);
  }
};


onMounted(loadPages);



let member_list = ref([])
function loadData(){
  axios.get(`http://localhost:8090/api/members/` + projectId, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  })
  .then((response) => {
    member_list.value = response.data;
  })
  .catch((err) => {
    console.log(err)
    expireToken(err, loadData)
  });
}
window.onload = loadData();

function show(userId) {
  axios.get('http://localhost:8090/api/user/' + userId, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  })
  .then((response) => {
    modalData.value = response.data;
    console.log(modalData.value)
  })
  .catch((err) => {
    console.log(err)
    expireToken(err, show(userId))
  });
}

function close() {
  modalData.value = null;
}
</script>

<style scoped>
@import "@/assets/css/sideModal.css"
</style>