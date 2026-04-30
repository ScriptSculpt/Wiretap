import { useEffect } from "react"

function App() {

  let count=0;

  useEffect(() => {
      count++;
      console.log(count);
      fetch('/api/history')
        .then(response => response.json())
        .then(data => console.log(data))
        .catch(error => console.error('Error fetching data:', error));
  }, []);

  return (
    <>
      <h1>Wiretap</h1>
    </>
  );
}

export default App;
