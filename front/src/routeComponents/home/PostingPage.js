import React, { useState,useContext,useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.css'; 
import HomeSelectContext from '../../components/home/HomeSelectContext';
import axios from 'axios';
import {useNavigate} from 'react-router-dom';
import BackButton from '../../components/home/BackButton';
import Swal from 'sweetalert2';

function PostingPage() {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [dorSelect, setDorSelect] = useState("기숙사");
    const [cateSelect, setCateSelect]=useState("카테고리");
    const token=localStorage.getItem('token');
    const navigate=useNavigate();
    const {selectComponentIndex,setSelectComponentIndex}=useContext(HomeSelectContext);

    useEffect(()=>{
      if(cateSelect==="카테고리" || dorSelect==="기숙사"){
        return ;
      }
      postArticle();
    },[cateSelect]);

    const dormitoryToId = {
        "오름1": 1,
        "오름2": 2,
        "오름3": 3, 
        "푸름1": 4,
        "푸름2": 5,
        "푸름3": 6,
        "푸름4": 7
      };
      
      const handleSwalDorm= async () => {
        const { value } = await Swal.fire({
          confirmButtonColor:"#FF8C00",
          title: "기숙사",
          confirmButtonText:"선택",
          cancelButtonText:"취소",
          input: "select",
          inputOptions: {
            오름1:"오름1",
            오름2:"오름2",
            오름3:"오름3",
            푸름1:"푸름1",
            푸름2:"푸름2",
            푸름3:"푸름3",
            푸름4:"푸름4"
          },
          inputPlaceholder: "기숙사를 선택해요.",
          showCancelButton: true,
          inputValidator: (value) => {
            return new Promise((resolve) => {
              if (value) {
                setDorSelect(value);
                resolve();
                

              } else {
                resolve("기숙사를 선택해주세요!");
              }
            });
          }
        });
      }

      const handleSwalCate= async () => {
        const { value } = await Swal.fire({
          confirmButtonColor:"#FF8C00",
          title: "카테고리",
          confirmButtonText:"선택",
          cancelButtonText:"취소",
          input: "select",
          inputOptions: {
            "족발•보쌈":"족발•보쌈",
            "찜•탕•찌개":"찜•탕•찌개",
            "돈까스•일식":"돈까스•일식",
            '피자':'피자',
            '고기•구이':'고기•구이',
            '백반•죽•국수':'백반•죽•국수',
            '양식':'양식',
            '치킨':'치킨',
            '중식':'중식',
            '아시안':'아시안',
            '도시락':'도시락',
            '분식':'분식',
            '카페•디저트':'카페•디저트',
            '패스트푸드':'패스트푸드'
          },
          inputPlaceholder: "카테고리를 선택해요.",
          showCancelButton: true,
          inputValidator: (value) => {
            return new Promise((resolve) => {
              if (value) {
                setCateSelect(value);
                resolve();
                
              } else {
                resolve("카테고리를 선택해주세요!");
              }
            });
          }
        });
      }

    const processNext = async () => {
        if(title==="" || content===""){
            alert("비워진 부분이 있어요! ");
            return;
        }
        
        handleSwalDorm()
        .then(()=>handleSwalCate());
    }

    const postArticle = async () => {
        console.log(dorSelect);
        console.log(cateSelect);

        const curTime=nowLocalDateTime();

        const fullPath = `http://localhost:8080/api/v1/article/new`;
        const data = {
          dorId: dormitoryToId[dorSelect],
          category:cateSelect,
          title:title,
          content:content,
          createTime:curTime
        };
      
        try {
        const response = await axios.post(fullPath, data, {
            headers: {
            'Authorization':`${token}`,
            }
        });
        
        navigate('/', {
            state: {
              from: '/',
              type: "success",
              message: "글을 올렸어요!"
            }
          });
          
        setCateSelect("카테고리");
        setDorSelect("기숙사");
        setSelectComponentIndex(0);
        window.location.reload();

         
        } catch (error) {
            if(error.response.data==="유효하지 않은 토큰입니다."){
                alert("회원 정보가 유효하지 않아요! 로그인해주세요.");
                navigate('/logIn',{state:{from:"/newWriting"}});
                
            }
        }
    }
    
    const nowLocalDateTime=()=>{
        const now=new Date();
        const localDateTime = now.getFullYear() + '-' +
        String(now.getMonth() + 1).padStart(2, '0') + '-' +
        String(now.getDate()).padStart(2, '0') + 'T' +
        String(now.getHours()).padStart(2, '0') + ':' +
        String(now.getMinutes()).padStart(2, '0') + ':' +
        String(now.getSeconds()).padStart(2, '0');
        
        return localDateTime;
    }
    
    return (
        <div className="App">
            <header className="App-postingPage-header">
                    <BackButton></BackButton>
                    <h6>글 쓰기</h6> 

                    <button type="button" className='btn btn-outline-primary'onClick={processNext}>다음</button>       
            </header>                 
            <main className="App-postingPage-main">
                <input type="text" value={title} placeholder='제목' style={{border:'none',outline:'none',width:'90%'}} onChange={e => setTitle(e.target.value)}  />
                <br/><br/>
                <textarea value={content} placeholder='내용을 입력하세요.' style={{border:'none',outline:'none',width:'90%',height:'90%'}} onChange={e => setContent(e.target.value)}  />
                <br />
            </main>
            

        </div>
        

    );
}

export default PostingPage;
