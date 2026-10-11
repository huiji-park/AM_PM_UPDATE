import { createRouter, createWebHistory } from "vue-router";

import Excel from "@/components/Excel/main.vue";
import DevPage from "@/pages/DevPage.vue";
import MainPage from "@/pages/MainPage.vue";
import TeamChat from "@/pages/TeamChat.vue";
import Chat from "@/pages/Chat.vue";
import MyPage from "@/pages/MyPage.vue";
import MainLayout from "../layouts/MainLayout.vue";
import CreateProjectLayout from "../layouts/CreateProjectLayout.vue";
import Login from "@/pages/Auth/login.vue";
import Register from "@/pages/Auth/register.vue";
import TestToken from "@/pages/Auth/testtoken.vue";
import Redirect from "@/pages/Auth/redirect.vue";
// import ProjectPlanPage from "@/pages/ProjectPlanPage.vue"
import QnaPage from "@/pages/QnaPage.vue";
import ProjectPlan from "@/components/ProjectPlan.vue";
import ProjectList from "@/pages/ProjectList.vue";
import CreateProject from "@/pages/CreateProject.vue";
import ModifyProject from "@/pages/ModifyProject.vue";
import AddMember from "@/pages/AddMember.vue";
import QnaDetail from "@/components/QnA/QnaDetail.vue";
import QnaWrite from "@/components/QnA/QnaWrite.vue";
import MemberList from "@/pages/MemberList.vue";
import LeaderChange from "@/pages/LeaderChange.vue";
const index = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: MainPage },
    {
      path: "/",
      component: MainLayout,
      children: [
        { path: "dev", component: DevPage },
        { path: "mypage", component: MyPage },
        { path: 'excel', component: Excel },

        { path: 'testtoken', component: TestToken },
        
      
        {
          path: '/:pageType',
          name: 'ProjectPlanPage',
          component: ProjectPlan,
          props: true 
        },

        /*
        { path: "excel", component: Excel },
        { path: "testtoken", component: TestToken },
        {
          path: "/:pageType/:projectId",
          name: "ProjectPlanPage",
          component: ProjectPlan,
          props: true 
        },
        */



        { path: "question", component: QnaPage},
        { path: "detail", component: QnaDetail},
        { path: "team-chat", component: TeamChat},
        { path: "/chat",
          component: Chat,
          name: "Chat"},
        { path: "project-list", component: ProjectList },
        { path: "create-project", component: CreateProject,
          props: true, // 모든 params를 props로 전달합니다.
        },
        { path: "question", component: QnaPage },
        { path: "detail", component: QnaDetail },
        { path: "team-chat", component: TeamChat },
        { path: "/chat", component: Chat, name: "Chat" },
        { path: "modify-project", component: ModifyProject },
        { path: "add-member", component: AddMember },
        { path: "write", component: QnaWrite },
        { path: "member-list", component: MemberList },
        { path: "leader-change", component: LeaderChange },
      ],
    },
    {
      path: "/",
      component: CreateProjectLayout,
      children: [
        { path: "create-project", component: CreateProject },
        { path: "project-list", component: ProjectList },
      ],
    },
    { path: "/login", component: Login },
    { path: "/register", component: Register },
    { path: "/oauth2/redirect", component: Redirect },
  ],
});

export default index;
