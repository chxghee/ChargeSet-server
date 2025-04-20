// pagination.js

export function renderPagination(pageData, currentPage, onClick) {
    const paginationEl = document.getElementById("pagination-list");
    paginationEl.innerHTML = '';

    const totalPages = pageData.totalPages;
    if (totalPages <= 1) return;

    const maxPageButtons = 5;
    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, startPage + maxPageButtons - 1);

    if (endPage - startPage < maxPageButtons - 1) {
        startPage = Math.max(0, endPage - (maxPageButtons - 1));
    }

    const createButton = (label, page, disabled = false, active = false) => {
        const li = document.createElement('li');
        li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;
        const btn = document.createElement('button');
        btn.className = 'page-link';
        btn.innerText = label;
        btn.disabled = disabled;
        btn.onclick = () => onClick(page);
        li.appendChild(btn);
        paginationEl.appendChild(li);
    };

    createButton('«', 0, currentPage === 0);
    createButton('‹', currentPage - 1, currentPage === 0);

    for (let i = startPage; i <= endPage; i++) {
        createButton(i + 1, i, false, i === currentPage);
    }

    createButton('›', currentPage + 1, currentPage >= totalPages - 1);
    createButton('»', totalPages - 1, currentPage >= totalPages - 1);
}
