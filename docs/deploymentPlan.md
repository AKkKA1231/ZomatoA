# Deployment Plan: Zomato AI Recommendation System

This document outlines the deployment strategy for both the frontend and the backend of the Zomato AI application.

## 1. Frontend Deployment (Vercel)

Vercel is the ideal platform for deploying modern web frontends (like the one generated via Google Stitch using React, Next.js, or Vite).

### Option A: Deploying via GitHub Integration (Recommended)
1. **Push your Frontend Code:** Commit and push the frontend codebase to a GitHub, GitLab, or Bitbucket repository.
2. **Import to Vercel:** Log in to [Vercel](https://vercel.com) and click **"Add New Project"**, then import your repository.
3. **Configure Environment Variables:** In the Vercel deployment settings, add an environment variable to point to your live backend URL once it's deployed.
   * Example: `NEXT_PUBLIC_API_URL=https://your-backend-service.onrender.com`
4. **Deploy:** Click **Deploy**. Vercel will automatically detect your framework (Next.js/Vite/React), build, and host your frontend on a global CDN.

### Option B: Deploying via Vercel CLI
1. Install the Vercel CLI: `npm i -g vercel`
2. Run `vercel` in your frontend directory to deploy to a preview URL.
3. Run `vercel --prod` to deploy to production.

### Handling CORS with `vercel.json`
To avoid Cross-Origin Resource Sharing (CORS) errors when your frontend communicates with the Spring Boot backend (hosted elsewhere), you can set up API rewrites in Vercel. 
Create a `vercel.json` file in your frontend root directory:
```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://your-backend-service.onrender.com/api/:path*"
    }
  ]
}
```
This allows your frontend code to make requests directly to `/api/v1/recommendations`, and Vercel will securely proxy it to your backend without triggering CORS issues in the browser.

---

## 2. Backend Deployment (Spring Boot)

**Crucial Limitation:** You **cannot** deploy the Spring Boot (Java) backend on Vercel. 
* Vercel is optimized for Serverless Functions (Node.js, Python, Go) and static sites. 
* Our backend requires a long-running Java 21 environment and loads a large processed dataset into memory, which exceeds Vercel's serverless execution limits and runtime constraints.

### Recommended Backend Hosts (Docker-based):
Since the backend uses an in-memory repository (loading `restaurants.json`), it needs a traditional containerized hosting service.

* **Render.com (Web Service):** Connect your GitHub repo, select "Docker" as the runtime, and Render will build and deploy the `Dockerfile` provided in the repository.
* **Railway.app:** Similar to Render, Railway easily deploys Dockerized Spring Boot apps.
* **Google Cloud Run:** A great scalable option where you push your Docker image and it runs continuously.

**Required Backend Environment Variables:**
* `GROQ_API_KEY`: Your LLM provider key.

---

## 3. Can we deploy this on Streamlit?

The answer depends on whether you mean the **Frontend** or the **Backend**.

### The Backend (No)
Streamlit Community Cloud is exclusively for hosting **Python** applications. Because our backend is written in Java (Spring Boot), it cannot be hosted on Streamlit. The backend must be hosted on a container service like Render or Railway.

### The Frontend (Yes!)
You can absolutely build the **Frontend** using Streamlit instead of React/Next.js. Streamlit is a fantastic, fast way to build data-driven UIs in Python.

**How it would work:**
1. Deploy the Spring Boot backend to Render or Railway.
2. Create a new Python project with a single `app.py` file using the `streamlit` and `requests` libraries.
3. In `app.py`, build the UI (dropdowns for City/Cuisine, sliders for Rating).
4. When the user clicks "Search", the Streamlit app makes a `requests.post()` call to your deployed Spring Boot API (`/api/v1/recommendations`).
5. Render the JSON response nicely in Streamlit using `st.write` or custom cards.
6. Deploy this `app.py` to **Streamlit Community Cloud** for free.
