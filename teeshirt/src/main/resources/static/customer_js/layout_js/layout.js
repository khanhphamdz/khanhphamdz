
window.addEventListener('scroll', function () {
    const btnBackToTop = document.getElementById('back-to-top');
    if (window.scrollY > 500) {
    btnBackToTop.style.display = 'block';
    } else {
    btnBackToTop.style.display = 'none';
    }
});
document.getElementById('back-to-top').addEventListener('click', function () {
    window.scrollTo({
    top: 0,
    behavior: 'smooth'
    });
});