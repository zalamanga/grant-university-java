<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Grant University | Portal Demo</title>
<link rel="stylesheet" href="${ctx}/assets/app.css">
<script src="${ctx}/assets/app.js" defer>
</script>
</head>
<body>
<a class="skip" href="#main">Lewati navigasi</a>
<header>
<div class="masthead">
<a class="brand" href="${ctx}/">Grant University<span>Portal kampus contoh</span>
</a>
<button class="menu-toggle" aria-expanded="false" aria-controls="navigation" hidden>Menu</button>
<p>Demonstrasi lokal<br>Gunakan data sintetis</p>
</div>
</header>
<div class="shell">
<aside>
<nav id="navigation" aria-label="Navigasi utama">
<a href="${ctx}/">Beranda</a>
<a href="${ctx}/about">Tentang portal</a>
<a href="${ctx}/courses">Program contoh</a>
<a href="${ctx}/admissions">Informasi pendaftaran</a>
<a href="${ctx}/contacts">Kontak demo</a>
<c:choose>
<c:when test="${not empty account}">
<a href="${ctx}/profile">Profil saya</a>
<form action="${ctx}/logout" method="post">
<%@ include file="csrf.jspf" %>
<button class="quiet">Keluar</button>
</form>
</c:when>
<c:otherwise>
<a href="${ctx}/register">Daftar akun demo</a>
<a href="${ctx}/login">Masuk</a>
</c:otherwise>
</c:choose>
</nav>
</aside>
<main id="main" tabindex="-1">
<c:if test="${not empty error}">
<div class="notice error" role="alert" tabindex="-1">
<c:out value="${error}"/>
</div>
</c:if>
<c:if test="${param.saved == '1'}">
<p class="notice" role="status">Perubahan tersimpan.</p>
</c:if>
<c:choose>
<c:when test="${page == 'home'}">
<section class="welcome">
<div>
<h1>Kenali program.<br>Kelola profilmu.</h1>
<p>Portal contoh Grant University untuk membaca informasi program dan mencoba pendaftaran akun serta profil mahasiswa.</p>
<p>Informasi program adalah data latihan dari proyek asal. Portal ini tidak mewakili kampus atau proses penerimaan yang aktif.</p>
<a class="button" href="${ctx}/courses">Lihat program contoh</a>
</div>
<img src="${ctx}/assets/education.jpg" alt="Ilustrasi globe dikelilingi kursi dari sumber proyek">
</section>
<section class="intro">
<h2>Akun yang dapat dicoba</h2>
<div>
<p>Daftar dengan data sintetis, masuk, lalu lihat dan ubah profil akunmu sendiri. Data tersimpan di database lokal.</p>
<a href="${ctx}/register">Daftar akun demo</a>
<a href="${ctx}/about">Baca penggunaan data</a>
</div>
</section>
</c:when>
<c:when test="${page == 'courses'}">
<h1>Program contoh</h1>
<p class="notice">Data berikut berasal dari contoh proyek lama. Durasi, syarat, dan biaya tidak menyatakan penawaran program atau tarif kampus yang berlaku.</p>
<c:if test="${empty courses}">
<p>Belum ada program contoh di database. Pengelola dapat mengisi kembali seed contoh pada database demo.</p>
</c:if>
<div class="courses">
<c:forEach var="course" items="${courses}">
<article>
<p class="code">
<c:out value="${course.id}"/>
</p>
<h2>
<c:out value="${course.name}"/>
</h2>
<p>Durasi contoh: <c:out value="${course.duration}"/>
</p>
<a href="${ctx}/course?id=${course.id}">Lihat rincian <c:out value="${course.name}"/>
</a>
</article>
</c:forEach>
</div>
</c:when>
<c:when test="${page == 'course'}">
<h1>
<c:out value="${course.name}"/>
</h1>
<p class="notice">Rincian program ini hanya contoh dari sumber lama, bukan informasi penerimaan atau biaya yang berlaku.</p>
<dl class="details">
<dt>Kode contoh</dt>
<dd><c:out value="${course.id}"/></dd>
<dt>Durasi</dt>
<dd><c:out value="${course.duration}"/></dd>
<dt>Syarat contoh</dt>
<dd><c:out value="${course.eligibility}"/></dd>
<dt>Biaya contoh</dt>
<dd><c:out value="${course.fee}"/></dd>
</dl>
<p>
<a class="button" href="${ctx}/courses">Kembali ke program contoh</a>
</p>
<a href="${ctx}/admissions">Baca batas pendaftaran</a>
</c:when>
<c:when test="${page == 'register' or page == 'profile-edit'}">
<c:set var="values" value="${page == 'register' or pageContext.request.method == 'POST' ? param : profile}"/>
<h1>${page == 'register' ? 'Daftar akun demo' : 'Ubah profil saya'}</h1>
<p>Gunakan data sintetis untuk semua kolom. Email dan nomor telepon tidak diverifikasi atau dihubungi.</p>
<form class="profile-form" action="${ctx}/${page == 'register' ? 'register' : 'profile/edit'}" method="post">
<%@ include file="csrf.jspf" %>
<div class="field-grid">
<%@ include file="profile-fields.jspf" %>
<c:if test="${page == 'register'}">
<div>
<label for="username">Username</label>
<input id="username" name="username" minlength="4" maxlength="40" pattern="[A-Za-z0-9_]{4,40}" autocomplete="username" required value="<c:out value='${param.username}'/>">
<p class="hint">4 sampai 40 huruf, angka, atau garis bawah.</p>
</div>
<div>
<label for="password">Kata sandi</label>
<input id="password" name="password" type="password" minlength="12" maxlength="128" autocomplete="new-password" required>
<p class="hint">12 sampai 128 karakter.</p>
</div>
<div>
<label for="confirm">Ulangi kata sandi</label>
<input id="confirm" name="confirm" type="password" minlength="12" maxlength="128" autocomplete="new-password" required>
</div>
</c:if>
</div>
<div class="actions">
<button>${page == 'register' ? 'Simpan akun demo' : 'Simpan profil'}</button>
<a href="${ctx}/${page == 'register' ? 'login' : 'profile'}">${page == 'register' ? 'Sudah punya akun? Masuk' : 'Batal'}</a>
</div>
</form>
</c:when>
<c:when test="${page == 'login'}">
<section class="form-page">
<h1>Masuk ke portal</h1>
<c:if test="${param.registered == '1'}">
<p class="notice" role="status">Akun dan profil tersimpan. Masuk untuk membuka profilmu.</p>
</c:if>
<form action="${ctx}/login" method="post">
<%@ include file="csrf.jspf" %>
<label for="username">Username</label>
<input id="username" name="username" maxlength="40" autocomplete="username" required value="<c:out value='${param.username}'/>">
<label for="password">Kata sandi</label>
<input id="password" name="password" type="password" maxlength="128" autocomplete="current-password" required>
<button>Masuk ke profil</button>
</form>
<p>
<a href="${ctx}/register">Daftar akun demo</a> · <a href="${ctx}/help">Bantuan akun</a>
</p>
</section>
</c:when>
<c:when test="${page == 'profile'}">
<div class="section-title">
<h1>Profil saya</h1>
<a class="button" href="${ctx}/profile/edit">Ubah profil</a>
</div>
<p class="demo-note">Profil ini milik akun yang sedang masuk. Isilah dengan data contoh.</p>
<dl class="details">
<dt>Username</dt>
<dd><c:out value="${profile.username}"/></dd>
<dt>Nama depan</dt>
<dd><c:out value="${profile.first_name}"/></dd>
<dt>Nama belakang</dt>
<dd><c:out value="${profile.last_name}"/></dd>
<dt>Email contoh</dt>
<dd><c:out value="${profile.email}"/></dd>
<dt>Tanggal lahir contoh</dt>
<dd><c:out value="${profile.birth_date}"/></dd>
<dt>Gender</dt>
<dd><c:out value="${profile.gender}"/></dd>
<dt>Telepon contoh</dt>
<dd><c:out value="${profile.phone}"/></dd>
<dt>Alamat contoh</dt>
<dd><c:out value="${profile.address}"/></dd>
</dl>
<section class="password-section form-page">
<h2>Ganti kata sandi</h2>
<form action="${ctx}/password" method="post">
<%@ include file="csrf.jspf" %>
<label for="current">Kata sandi saat ini</label>
<input id="current" name="currentPassword" type="password" maxlength="128" autocomplete="current-password" required>
<label for="password">Kata sandi baru</label>
<input id="password" name="password" type="password" minlength="12" maxlength="128" autocomplete="new-password" required>
<p class="hint">12 sampai 128 karakter.</p>
<button>Ganti kata sandi</button>
</form>
</section>
</c:when>
<c:when test="${page == 'about'}">
<section class="prose">
<h1>Tentang portal &amp; data</h1>
<p>Grant University adalah nama dalam proyek latihan portal kampus asal. Versi ini mempertahankan nama, warna navy/putih, ilustrasi pendidikan, serta empat program contoh.</p>
<h2>Data akun lokal</h2>
<p>Database menyimpan username, nama, email contoh, hash kata sandi, tanggal lahir contoh, keterangan gender, alamat contoh, dan nomor telepon contoh. Profil hanya dapat dilihat dan diubah oleh akun yang sedang masuk. Tidak ada daftar profil mahasiswa publik.</p>
<p>Gunakan data sintetis. Pengelola instalasi dapat menghapus akun melalui database lokal; profil terkait ikut terhapus. Pengelola bertanggung jawab atas backup, akses database, dan penghapusan data instalasinya.</p>
<h2>Batas portal</h2>
<p>Tidak ada kampus aktif, pengajuan penerimaan, pembayaran, verifikasi email, pengiriman pesan, unggahan dokumen, atau pemulihan kata sandi otomatis. Tahun pendirian, jumlah mahasiswa, tanggal penerimaan lama, dan alamat kontak sumber tidak disajikan sebagai fakta.</p>
<h2>Modernisasi sumber</h2>
<p>Halaman asal memakai JSF dengan login dan profil yang ditulis langsung di kode. Versi ini memakai Servlet/JSP dan JDBC dengan data akun yang tersimpan. Tidak ditemukan lisensi atau nama pembuat yang dapat dipastikan pada sumber lokal.</p>
</section>
</c:when>
<c:when test="${page == 'admissions'}">
<section class="prose">
<h1>Informasi pendaftaran</h1>
<p>Portal ini menyediakan pendaftaran akun demo, bukan pendaftaran kuliah. Tidak ada penerimaan mahasiswa atau jadwal pendaftaran yang sedang dibuka.</p>
<p>Empat program contoh mempertahankan nilai latihan dari source lama. Halaman tanggal penerimaan tahun 2011 pada sumber tidak diperlakukan sebagai jadwal saat ini.</p>
<a class="button" href="${ctx}/register">Daftar akun demo</a>
<a href="${ctx}/courses">Lihat program contoh</a>
</section>
</c:when>
<c:when test="${page == 'contacts'}">
<section class="prose">
<h1>Kontak demo</h1>
<p>Portal ini tidak mempunyai layanan kontak kampus. Alamat, telepon, dan email pada source lama tidak diverifikasi dan tidak digunakan untuk menghubungi siapa pun.</p>
<p>Untuk mencoba aplikasi, gunakan halaman program contoh atau buat akun demo lokal. Tidak ada formulir pesan yang mengirim email.</p>
<a class="button" href="${ctx}/courses">Buka program contoh</a>
</section>
</c:when>
<c:when test="${page == 'help'}">
<section class="prose">
<h1>Bantuan akun</h1>
<p>Masuk menggunakan username yang kamu daftarkan. Untuk mengganti kata sandi, buka Profil saya dan isi kata sandi saat ini serta kata sandi baru.</p>
<p>Tidak ada pengiriman email atau pemulihan otomatis. Jika lupa kata sandi, buat akun demo baru dengan username dan email contoh yang berbeda.</p>
<a href="${ctx}/login">Kembali ke Masuk</a>
</section>
</c:when>
<c:otherwise>
<section class="prose">
<h1>Data belum dapat ditampilkan</h1>
<p>Coba muat ulang setelah layanan database siap.</p>
<a class="button" href="${ctx}/courses">Coba buka program contoh</a>
<a href="${ctx}/">Kembali ke beranda</a>
</section>
</c:otherwise>
</c:choose>
</main>
</div>
<footer>
<strong>Grant University</strong>
<p>Portal kampus contoh. Informasi sumber bukan penawaran program aktif.</p>
<a href="${ctx}/about">Data &amp; batas demo</a>
</footer>
<p class="submit-status" role="status" aria-live="polite" hidden>Menyimpan perubahan…</p>
</body>
</html>
