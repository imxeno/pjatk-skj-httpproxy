const unpleasantWords = [/*UNPLEASANT_WORDS_HERE*/];

function replaceInText(element, pattern, replacement) {
    for (let node of element.childNodes) {
        switch (node.nodeType) {
            case Node.ELEMENT_NODE:
                replaceInText(node, pattern, replacement);
                break;
            case Node.TEXT_NODE:
                node.parentElement.innerHTML = node.parentElement.innerHTML
                    .replace(node.textContent, node.textContent.replace(pattern, replacement));
                break;
            case Node.DOCUMENT_NODE:
                replaceInText(node, pattern, replacement);
        }
    }
}

window.onload = () => {
    const rgx = new RegExp("(" + unpleasantWords.join("|") + ")", "g");
    replaceInText(document.body, rgx, "<span class='s19880-unpleasantWord'>$1</span>");
    document.body.innerHTML += "<style>.s19880-unpleasantWord{color: red;}</style>";
};