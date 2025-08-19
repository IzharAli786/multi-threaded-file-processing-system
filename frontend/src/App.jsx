import { useState } from 'react'
import './App.css'
import axios from 'axios';

function App() {
   const [file ,setFile] = useState(null);
   const [successMessage, setSuccessMessage] = useState('');
   const [errorMessage, setErrorMessage] = useState('');
   const [fileMessage , setFileMessage] = useState('');
   const [loading, setLoading] = useState(false);

   const handleFileClick = async (event) => {
       event.preventDefault();
       try {
           setLoading(true);
           if (!file) {
               setErrorMessage("no file selected");
               return;
           }
           const formdata = new FormData();
           formdata.append("file",file);
           const response = await axios.post('/users/upload/files', formdata,{
               headers: {'Content-Type': 'multipart/form-data'},
               responseType:"blob"

           })
           if(response.status === 200){
               const blob= new Blob([response.data],{
                   type:"application/vnd.openxmlformats-officedocument.wordprocessingml.document"
               });
               const url= window.URL.createObjectURL(blob);
               const link = document.createElement("a");
               link.href=url;
               link.download= file.name.replace(".pdf",".docx");
               document.body.appendChild(link);
               link.click();
               document.body.removeChild(link);
               window.URL.revokeObjectURL(url);
               setFile(null);
               setSuccessMessage("file successfully converted to pdf");
           }

       }catch(error){
         if(error.response){
             setErrorMessage(error.response.data.message);
         }
        else {
            setErrorMessage("Something went wrong");
         }

       }    finally {
           setLoading(false);
       }

   }
   const handleFileUpload=(event) => {
       const f= event.target.files?.[0];
       setFile(f || null);
       setErrorMessage(null);
       setSuccessMessage(null);
   }

   return (
       <>
           <input type="file" onChange={handleFileUpload} accept={"application/pdf"}></input>
           <button onClick={ handleFileClick} type="button" disabled={loading} >
               {loading?"Uploading":"Upload"}
           </button>
           <p>{successMessage}</p>
            <p>{fileMessage}</p>
       </>
   )
}

export default App;