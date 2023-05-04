import{W as t}from"./index-c4f0b7db.js";const m=t("store",{state:()=>({submissionIdsToComparisonFileName:new Map,anonymous:new Set,files:{},submissions:{},local:!1,zip:!1,single:!1,fileString:"",fileIdToDisplayName:new Map}),getters:{filesOfSubmission:s=>e=>Array.from(s.submissions[e],([o,i])=>({name:o,value:i})),submissionDisplayName:s=>e=>s.fileIdToDisplayName.get(e),getSubmissionIds(s){return Array.from(s.fileIdToDisplayName.keys())},getComparisonFileName:s=>(e,o)=>{var i;return(i=s.submissionIdsToComparisonFileName.get(e))==null?void 0:i.get(o)},getComparisonFileForSubmissions(){return(s,e)=>{const o=this.getComparisonFileName(s,e),i=o?Object.keys(this.files).find(n=>n.endsWith(o)):void 0;return i!=null?this.files[i]:void 0}}},actions:{clearStore(){this.$reset()},addAnonymous(s){for(let e=0;e<s.length;e++)this.anonymous.add(s[e])},removeAnonymous(s){for(let e=0;e<s.length;e++)this.anonymous.delete(s[e])},resetAnonymous(){this.anonymous=new Set},saveComparisonFileLookup(s){this.submissionIdsToComparisonFileName=s},saveFile(s){this.files[s.fileName]=s.data},saveSubmissionNames(s){this.fileIdToDisplayName=s},saveSubmissionFile(s){this.submissions[s.name]||(this.submissions[s.name]=new Map),this.submissions[s.name].set(s.file.fileName,s.file.data)},setLoadingType(s){this.local=s.local,this.zip=s.zip,this.single=s.single,this.fileString=s.fileString}}});export{m as s};
