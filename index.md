---
layout: default
title: Sankar's Tech Explorations
published: true
---
### Tech Explorations
<ul>
  {% for post in site.posts %}
    <li>
      <a href="{{ post.url }}">{{ post.title }}</a>
    </li>
  {% endfor %}
</ul>


