
const cdn = "https://cdn.jsdelivr.net/npm/sudu-editor-tmp@0.0.8-beta5"
const editorJs = "/src/editor.js";
const workerJS = "/src/worker.js"

const ep = import(cdn + editorJs)
const wp = fetch(cdn + workerJS).then(r => r.blob());
await Promise.all([ep, wp]);
const editorApi = await ep;
const workerBlob = await wp;

let workerUrl = URL.createObjectURL(workerBlob);
const editor = await editorApi.newEditor({containerId: "editor", workerUrl: workerUrl});
URL.revokeObjectURL(workerUrl);

editor.setText("loaded from " + cdn + workerJS)

const input = document.getElementById("address");

input.onkeydown = (event) => {
    if (event.key === 'Enter') editor.setText(input.value)
};

document.addEventListener("DOMContentLoaded", () => console.log("DOMContentLoaded"))
