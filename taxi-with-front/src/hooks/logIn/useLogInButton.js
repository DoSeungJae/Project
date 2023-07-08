import Button from '../../components/common/Button.js';
import {useState} from 'react';
import InputForm from '../../components/common/InputForm.js';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/js/bootstrap.js';

function UseLogInButton() {
    const [id, setId] = useState('');
    const [pw, setPw] = useState('');
    const [idError,setIdError]=useState(false);
    const [pwError,setPwError]=useState(false);
  
    const buttonPressed = () => {
      if(id===''){
        setIdError(true);

      }
      if(pw===''){
        setPwError(true);
      }

      else{
        console.log(id);
        console.log(pw);
        
      }
      
    };
  
    return (
      <div class="container">
        <div class="row mt-5">
          <InputForm
            type="text"
            placeholder="아이디"
            value={id}
            onChange={(e) => setId(e.target.value)}
          />
          {idError && <small class="mt-2">아이디를 입력해주세요.</small>}
        </div>
        <div class="row mt-3">
          <InputForm
            type="password"
            placeholder="비밀번호"
            value={pw}
            onChange={(e) => setPw(e.target.value)}
          />
          {pwError && <small class="mt-2">비밀번호를 입력해주세요.</small>}
        </div>
  
        <div class="d-grid mt-4 pb-4">
          <Button onClick={buttonPressed}>로그인</Button>
        </div>
      </div>
    );
  }
  
  export default UseLogInButton;

