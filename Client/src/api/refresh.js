import axios from 'axios';

export const refresh = () => {
    axios.get("http://localhost:8090/api/rtoken", {
        headers: { 
            "RefreshToken" : sessionStorage.getItem("refresh-token"),
            "Authorization" : sessionStorage.getItem("access-token") }
    }).then(response => {
        console.log(response)
        if(response.status == 200){
            console.log("토큰 재발급");
            console.log(response.headers.authorization);
            sessionStorage.setItem("access-token", response.headers.authorization);
        } else {
            console.log("토큰 재발급 실패");
        }
    }).catch(error => {console.error(error);})
}

