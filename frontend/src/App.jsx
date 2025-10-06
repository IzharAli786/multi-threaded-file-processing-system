import {useState} from 'react'
import './App.css'
import axios from 'axios';
import './index.css';
import WebSocket from "./webSockets/WebSocket.jsx";

function App() {
    const [file, setFile] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [fileMessage, setFileMessage] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleFileClick = async (event) => {
        event.preventDefault();
        console.log('Convert button clicked, current file:', file); // Debug log

        try {
            setLoading(true);
            setErrorMessage(''); // Clear previous errors
            setSuccessMessage(''); // Clear previous success

            if (!file) {
                console.log('No file found!'); // Debug log
                setErrorMessage("No file selected");
                return;
            }

            const formData = new FormData();
            file.forEach(file => {
                formData.append("file", file);
            })
            const response = await axios.post('/customer/upload/files', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': 'Basic ' + btoa('izhar:izhar')
                },
                responseType: "blob"
            });

            if (response.status === 200) {
                const blob = new Blob([response.data], {
                    type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                });


                const url = window.URL.createObjectURL(blob);
                file.forEach(file => {
                    const link = document.createElement("a");
                    link.href = url;
                    link.download = file.name.replace(".pdf", ".docx");
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                })
                window.URL.revokeObjectURL(url);
                setFile(null);
                setSuccessMessage("File successfully converted to Word document");
            }
        } catch (error) {
            console.error('Error during conversion:', error); // Debug log
            if (error.response) {
                setErrorMessage(error.response.data.message || 'Server error occurred');
            } else if (error.request) {
                setErrorMessage('Network error - please check your connection');
            } else {
                setErrorMessage("Something went wrong during conversion");
            }
        } finally {
            setLoading(false);
        }
    }

    const handleFileUpload = (event) => {
        const fileArray = Array.from(event.target.files);

        setFile(fileArray || null);
        setErrorMessage(''); // Clear previous errors
        setSuccessMessage(''); // Clear previous success
        if (fileArray.length >= 1) {
            const names = fileArray.map(file => file.name);
            setFileMessage(names);
        } else {
            setFileMessage(null);
        }
    }
    return (
        <>
            <div
                className="min-h-screen bg-gradient-to-br from-gray-900 via-slate-900 to-black flex items-center justify-center p-4 overflow-hidden">

                {/* Floating background elements */}
                <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    <div
                        className="absolute top-20 right-20 w-32 h-32 bg-gradient-to-r from-blue-500/20 to-purple-500/20 rounded-full opacity-30 animate-pulse blur-xl"></div>
                    <div
                        className="absolute bottom-20 left-20 w-24 h-24 bg-gradient-to-r from-pink-500/20 to-indigo-500/20 rounded-full opacity-30 animate-pulse blur-xl"></div>
                    <div
                        className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-40 h-40 bg-gradient-to-r from-cyan-500/10 to-violet-500/10 rounded-full opacity-20 animate-pulse blur-2xl"></div>
                </div>

                <div
                    className="relative bg-gray-800/90 backdrop-blur-sm rounded-2xl shadow-2xl border border-gray-700/50 p-6 w-full max-w-sm ring-1 ring-white/5">
                    {/* Header */}
                    <div className="text-center mb-6">
                        <div
                            className="inline-flex items-center justify-center w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl mb-3 shadow-lg ring-2 ring-blue-500/20">
                            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                            </svg>
                        </div>
                        <h2 className="text-xl font-bold text-white mb-1">PDF to Word</h2>
                        <p className="text-gray-400 text-sm">Convert your files instantly</p>
                    </div>

                    {/* Upload Area */}
                    <div
                        className="border-2 border-dashed border-gray-600 rounded-xl p-6 text-center transition-all duration-300 cursor-pointer hover:border-blue-400 hover:bg-blue-500/5 group mb-4 relative bg-gray-900/50">
                        <input
                            type="file" multiple={true}
                            onChange={handleFileUpload}
                            accept="application/pdf"
                            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                        />

                        <svg className="w-8 h-8 text-gray-400 group-hover:text-blue-400 transition-colors mx-auto mb-2"
                             fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
                        </svg>

                        <p className="text-sm font-medium text-gray-200">Drop PDF here</p>
                        <p className="text-xs text-gray-500 mt-1">or click to browse</p>

                        {file && (
                            <div className="mt-2 p-2 bg-blue-500/10 rounded-lg border border-blue-500/20">
                                <p className="text-xs text-blue-300 font-medium truncate">{file.name}</p>
                                <p className="text-xs text-blue-400">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
                            </div>
                        )}
                    </div>

                    {/* Upload Button */}
                    <button
                        onClick={handleFileClick}
                        type="button"
                        disabled={loading || !file}
                        className={`w-full py-3 px-4 rounded-xl font-medium text-white transition-all duration-300 shadow-lg ${
                            loading || !file
                                ? "bg-gray-600 cursor-not-allowed opacity-50"
                                : "bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 hover:shadow-blue-500/25 hover:shadow-xl active:scale-95 ring-2 ring-blue-500/20"
                        }`}
                    >
                        {loading ? (
                            <span className="flex items-center justify-center">
                                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none"
                                     viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                            strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor"
                                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Converting...
                            </span>
                        ) : !file ? (
                            "Select a PDF file first"
                        ) : (
                            "Convert to Word"
                        )}
                    </button>

                    {/* Success Message */}
                    {successMessage && (
                        <div
                            className="mt-3 p-3 bg-green-500/10 border border-green-500/30 rounded-xl backdrop-blur-sm">
                            <p className="text-green-300 text-sm text-center flex items-center justify-center">
                                <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor"
                                     viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M5 13l4 4L19 7"/>
                                </svg>
                                {successMessage}
                            </p>
                        </div>
                    )}

                    {/* Error Message */}
                    {errorMessage && (
                        <div className="mt-3 p-3 bg-red-500/10 border border-red-500/30 rounded-xl backdrop-blur-sm">
                            <p className="text-red-300 text-sm text-center flex items-center justify-center">
                                <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor"
                                     viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                </svg>
                                {errorMessage}
                            </p>
                        </div>
                    )}

                    {/* File Message */}
                    {fileMessage && (

                        fileMessage.map((file, i) => (
                            <div
                                className="mt-3 flex flex-column p-3 bg-blue-500/10 border border-blue-500/30 rounded-xl backdrop-blur-sm">
                                <p className="text-blue-300 text-sm text-center">
                                    {file}
                                </p>
                            </div>
                        ))

                    )}
                </div>
            </div>
            <WebSocket></WebSocket>
        </>
    )
}

export default App;
